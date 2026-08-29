# Kubernetes

Os manifestos usam Kustomize e separam a configuração comum dos ambientes local e de produção.

```text
k8s/
├── base/                         # 6 Deployments, Services, ConfigMaps e HPAs
├── overlays/
│   ├── local/                    # Secrets locais + imagens locais
│   └── production/               # registry e banco externos
└── addons/metrics-server/        # instalação separada por ser cluster-wide
```

O PostgreSQL do ambiente local não faz mais parte deste diretório: ele é provisionado pelo Terraform
em `/infra` (ver `infra/README.md`), atendendo ao requisito do PDF de que o banco de dados seja
criado via IaC. `kubectl apply -k k8s/overlays/local` assume que o Service `postgres-mecanica` já
existe no namespace — ou seja, rode `terraform apply` em `/infra` antes.

## O que está configurado

- Services das APIs do tipo `ClusterIP`, nas portas 8081 a 8086.
- Init container aguarda o PostgreSQL aceitar conexões antes de iniciar cada API.
- Probes de startup, liveness e readiness pelo Spring Boot Actuator; o readiness também verifica o PostgreSQL.
- Shutdown gracioso: 30 segundos no Spring e 45 segundos no Pod.
- HPA `autoscaling/v2`: CPU 70%, memória 75% e máximo de 5 réplicas.
- Mínimo de 1 réplica no ambiente local e 2 em produção.
- PostgreSQL 16 com PVC de 2 Gi no ambiente local, provisionado pelo Terraform (`/infra`).
- Metrics Server v0.9.0, compatível com Kubernetes 1.34 ou superior.

Recursos iniciais:

| Serviços | Requests | Limits |
|---|---|---|
| cliente, veiculo, funcionario, servico e estoque | 100m CPU / 384Mi | 500m CPU / 512Mi |
| atendimento | 200m CPU / 512Mi | 750m CPU / 768Mi |

Esses valores consideram o consumo ocioso medido nos containers atuais. Ajuste-os após testes de carga e observação em produção.

## Ambiente local (kind via Terraform)

Pré-requisitos:

- Docker rodando;
- `terraform`, `kind` e `kubectl` instalados;
- `.env` do Compose configurado.

Provisione o cluster kind e o PostgreSQL com Terraform (detalhes em `infra/README.md`):

```powershell
cd infra
terraform init
terraform apply
cd ..
```

Construa as seis imagens locais e carregue-as no cluster kind (ele não enxerga o cache de imagens do host):

```powershell
docker compose -f compose.app.yaml build
kind load docker-image mecanica/cliente:local mecanica/veiculo:local mecanica/funcionario:local mecanica/servico:local mecanica/estoque:local mecanica/atendimento:local --name mecanica
```

Crie os arquivos locais de Secret. Eles são ignorados pelo Git:

```powershell
Copy-Item k8s/overlays/local/secrets/shared.env.example k8s/overlays/local/secrets/shared.env
Copy-Item k8s/overlays/local/secrets/atendimento.env.example k8s/overlays/local/secrets/atendimento.env
Copy-Item k8s/overlays/local/secrets/external-services.env.example k8s/overlays/local/secrets/external-services.env
```

Os exemplos contêm apenas credenciais de sandbox e já batem com o default do Terraform em `/infra`. Troque os valores dos dois lados (aqui e em `infra/variables.tf`/`-var`) se o cluster local for compartilhado.

Instale o Metrics Server e depois a aplicação:

```powershell
kubectl apply -k k8s/addons/metrics-server/overlays/local
kubectl apply -k k8s/overlays/local
kubectl wait --for=condition=available deployment --all -n mecanica --timeout=300s
```

O overlay local adiciona `--kubelet-insecure-tls` ao Metrics Server, necessário porque o kind não expõe um certificado do kubelet válido para a cadeia padrão. Essa opção não existe no overlay de produção.

Confira os recursos e as métricas:

```powershell
kubectl get pods,services,hpa -n mecanica
kubectl top pods -n mecanica
```

Como os Services são `ClusterIP`, use port-forward para acessar uma API a partir da máquina. Exemplo para atendimento:

```powershell
kubectl port-forward service/atendimento 8086:8086 -n mecanica
```

Se o Compose ainda estiver usando as portas 8081 a 8086, encerre-o antes do port-forward com `docker compose -f compose.app.yaml down`. Para usar toda a collection do Postman, abra um terminal para cada Service e encaminhe também as portas 8081 a 8085.

Para remover só a aplicação (Deployments/Services/HPA/Secrets), mantendo o cluster e o PostgreSQL no ar:

```powershell
kubectl delete -k k8s/overlays/local
kubectl delete -k k8s/addons/metrics-server/overlays/local
```

**Atenção**: esse comando também remove o Namespace `mecanica` (ele é um recurso de `k8s/base`), o que
por cascata apaga o PostgreSQL provisionado pelo Terraform sem passar pelo `terraform destroy` — o
estado do Terraform fica dessincronizado (ele ainda "acha" que os recursos existem). Se a intenção é
recriar só a aplicação, prefira `kubectl delete deployment,service,hpa --all -n mecanica` (não apaga o
namespace). Para uma remoção completa e consistente, destrua pelo Terraform — ele já derruba o cluster
kind inteiro, dispensando o `kubectl delete` acima:

```powershell
cd infra
terraform destroy
```

## Secrets

O overlay local gera objetos `Secret` a partir dos arquivos `.env` ignorados. Os Deployments esperam estes contratos:

| Secret | Chaves |
|---|---|
| `mecanica-shared-secrets` | `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET` |
| `atendimento-secrets` | `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD` |
| `external-services-secrets` | `EXTERNAL_SERVICE_TOKEN` reservado para integrações futuras |

O `JWT_SECRET` deve ser igual nos seis serviços e ter pelo menos 32 bytes. O token externo ainda não é consumido pela aplicação e, por isso, não é injetado nos Pods.

Em um PVC já inicializado, trocar `DATABASE_PASSWORD` no Secret não altera automaticamente a senha interna do PostgreSQL. Da mesma forma, `ADMIN_PASSWORD` só vale na criação inicial do administrador; rotações exigem atualização coordenada no banco.

Kubernetes Secrets em Base64 não são criptografia. Em produção, crie `mecanica-shared-secrets` e `atendimento-secrets` antes do deploy usando o gerenciador de segredos da nuvem ou External Secrets. Valores reais não devem entrar no Git.

## Produção

Antes de aplicar o overlay de produção:

1. Troque `postgres.example.internal` pelo DNS do banco em `overlays/production/database/postgresql-external-service.yaml`.
2. Troque o issuer de exemplo no patch `overlays/production/patches/atendimento-config.yaml`.
3. Defina registry e tags imutáveis das seis imagens em `overlays/production/kustomization.yaml`.
4. Configure host/porta de SMTP e endereços de notificação no ConfigMap de atendimento.
5. Provisione no banco os seis bancos lógicos usados nas URLs de datasource.
6. Faça o gerenciador de segredos materializar os Secrets no namespace `mecanica-production`.

Depois, aplique:

```powershell
kubectl apply -k k8s/addons/metrics-server/overlays/production
kubectl apply -k k8s/overlays/production
kubectl wait --for=condition=available deployment --all -n mecanica-production --timeout=300s
```

O overlay de produção não cria PostgreSQL nem persiste credenciais. O Service `postgres-mecanica` é apenas um alias DNS para o banco externo provisionado na etapa de infraestrutura.

## Validação dos manifestos

Sem acessar um cluster, valide a renderização:

```powershell
kubectl kustomize k8s/base
kubectl kustomize k8s/overlays/local
kubectl kustomize k8s/overlays/production
kubectl kustomize k8s/addons/metrics-server/overlays/local
kubectl kustomize k8s/addons/metrics-server/overlays/production
```

Com o cluster ativo, valide contra a API antes de aplicar:

```powershell
kubectl apply --dry-run=server -k k8s/overlays/local
```

## Limitações antes de produção real

- `ddl-auto: update` continua ativo. Com múltiplas réplicas, inicializações simultâneas podem disputar alterações de schema; migrações versionadas devem ser adotadas antes de produção.
- O inicializador do administrador pode executar em duas réplicas de atendimento ao mesmo tempo. Ele deve se tornar atômico ou ser movido para um Job/migração.
- HPA de memória em aplicações JVM pode reduzir réplicas lentamente; calibre os limites com métricas reais.
- O script SQL local só executa quando o PVC do PostgreSQL está vazio.

O manifesto do Metrics Server foi fixado na versão v0.9.0 a partir do [repositório oficial](https://github.com/kubernetes-sigs/metrics-server/releases/tag/v0.9.0).
