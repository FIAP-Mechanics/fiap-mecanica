# Oficina Mecânica

Monorepo de microsserviços para gerenciamento de uma oficina mecânica. O sistema cobre cadastro de clientes, veículos, serviços, estoque/insumos, abertura e acompanhamento de ordens de serviço, cálculo de orçamento, notificações, autenticação JWT e controle de permissões por perfil.

O projeto está organizado como um monorepo Maven multi-módulo, sem API Gateway: cada serviço é acessado diretamente pela sua própria porta.

## Objetivo

O objetivo do projeto é apoiar o fluxo principal de atendimento de uma oficina, desde a chegada do cliente até a entrega do veículo, mantendo rastreabilidade da ordem de serviço e separando as responsabilidades entre atendente, mecânico, administrador e cliente.

## Fase 2

Nesta fase, a solução da oficina foi evoluída para atender aos requisitos de arquitetura, operação e entrega contínua do Tech Challenge:

- os seis microsserviços seguem Clean Architecture, separando domínio, casos de uso, portas e adaptadores;
- as APIs cobrem abertura e consulta da ordem de serviço, decisão externa sobre o orçamento, ordenação operacional das ordens e notificações por e-mail;
- o ambiente de desenvolvimento completo é executado com Docker e Docker Compose;
- os Deployments, Services, ConfigMaps, Secrets, probes e HPAs são declarados em manifestos Kubernetes com Kustomize;
- o Terraform provisiona o cluster Kubernetes local e o PostgreSQL;
- os workflows do GitHub Actions validam os serviços, publicam as imagens Docker e implantam a aplicação em um cluster efêmero.

Os guias de execução estão nas seções [Docker](#subindo-toda-a-aplicação-com-docker), [Kubernetes](#kubernetes), [Terraform](#infraestrutura-como-código-terraform) e [CI/CD](#cicd).

## Microsserviços

| Serviço                    | Porta | Banco (Docker)              | Responsabilidade                                          |
|-----------------------------|-------|------------------------------|-------------------------------------------------------------|
| `services/cliente`          | 8081  | `postgres-cliente:5432`      | Cadastro de clientes                                         |
| `services/veiculo`          | 8082  | `postgres-veiculo:5433`      | Cadastro de veículos                                          |
| `services/funcionario`      | 8083  | `postgres-funcionario:5434`  | Cadastro de funcionários                                      |
| `services/servico`          | 8084  | `postgres-servico:5435`      | Cadastro de tipos de serviço                                  |
| `services/estoque`          | 8085  | `postgres-estoque:5436`      | Insumos e controle de estoque                                 |
| `services/atendimento`      | 8086  | `postgres-atendimento:5437`  | Ordens de serviço, autenticação JWT e notificações por e-mail |

Cada serviço em `services/<nome>` é um módulo Maven independente, com seu próprio `pom.xml`, `compose.yaml`, banco de dados e ciclo de vida. Não há Gateway/proxy: os clientes das APIs acessam cada serviço diretamente pela sua porta.

O serviço `atendimento` concentra a autenticação do sistema (`POST /auth/login`) e consome os demais serviços (`cliente`, `veiculo`, `servico`, `estoque`) via HTTP (`RestClient`).

## Fluxo da Ordem de Serviço

Todos os endpoints abaixo são expostos pelo serviço `atendimento` (porta 8086).

1. O cliente solicita o atendimento para a atendente.
2. A atendente identifica ou cadastra o cliente (`services/cliente`, porta 8081) e o veículo (`services/veiculo`, porta 8082).
3. A atendente inicia a ordem de serviço em `POST /atendimento/iniciar`.
4. A ordem nasce com status `RECEBIDA`.
5. O mecânico inicia o diagnóstico em `PATCH /atendimento/{id}/diagnostico/iniciar`.
6. O mecânico adiciona diagnóstico, serviços e insumos em `POST /atendimento/{id}/diagnostico`.
7. O sistema calcula o orçamento e altera a OS para `AGUARDANDO_APROVACAO`.
8. O cliente acompanha a OS por `GET /atendimento/{id}` e a aplicação externa comunica sua decisão em `POST /atendimento/{id}/decisao-orcamento`, enviando `{"aprovado": true}` ou `{"aprovado": false}`.
9. Se aprovado, o sistema baixa os insumos do estoque (`services/estoque`, porta 8085) e muda a OS para `EM_EXECUCAO`.
10. Se recusado, a ordem é cancelada logicamente.
11. O mecânico finaliza a execução em `POST /atendimento/{id}/finalizar`, informando o tempo gasto nos serviços.
12. A atendente entrega o veículo em `POST /atendimento/{id}/entregar`.

## Status da OS

- `RECEBIDA`: ordem criada e aguardando diagnóstico.
- `EM_DIAGNOSTICO`: veículo em avaliação técnica.
- `AGUARDANDO_APROVACAO`: orçamento calculado e aguardando retorno do cliente.
- `EM_EXECUCAO`: orçamento aprovado e serviço em andamento.
- `FINALIZADA`: serviço técnico concluído.
- `CANCELADA`: ordem cancelada após recusa do orçamento.
- `ENTREGUE`: veículo entregue ao cliente.

## Notificações

O serviço `atendimento` envia e-mails ao cliente durante o fluxo da OS e ao administrador quando falta estoque. Os templates necessários estão na pasta `06 - Templates` da collection do Postman.

Para desabilitar os e-mails, use no `.env`:

```dotenv
NOTIFICACAO_EMAIL_REMETENTE=skip
NOTIFICACAO_EMAIL_ADMIN=skip
```

Para habilitar, configure o SMTP e substitua `skip` por endereços válidos:

```dotenv
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=usuario-smtp
MAIL_PASSWORD=senha-smtp
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
NOTIFICACAO_EMAIL_REMETENTE=no-reply@oficina.com
NOTIFICACAO_EMAIL_ADMIN=estoque@oficina.com
```

Após alterar o `.env`, recrie o container:

```powershell
docker compose -f compose.app.yaml up -d --force-recreate atendimento
```

O e-mail do cliente é obtido automaticamente do cadastro. Falhas do SMTP são registradas nos logs e não interrompem a operação principal.

## Perfis e Permissões

As APIs administrativas usam JWT emitido pelo serviço `atendimento`. O token carrega a role do funcionário autenticado.

- `ADMIN`: acesso total ao sistema, incluindo funcionários, templates, relatórios e operações administrativas.
- `ATENDENTE`: opera clientes, veículos, estoque, abertura de atendimento, aprovação de orçamento, cancelamento e entrega da OS.
- `MECANICO`: opera serviços, estoque, diagnóstico, inclusão de serviços/insumos na OS e finalização técnica.
- Cliente: não possui login neste projeto; acompanha a OS pelo endpoint público `GET /atendimento/{id}`.

Endpoints públicos:

- `POST /auth/login` (`services/atendimento`, porta 8086)
- `GET /atendimento/{id}` (`services/atendimento`, porta 8086)
- Todos os endpoints `GET` dos demais serviços (`cliente`, `veiculo`, `funcionario`, `servico`, `estoque`)
- `/actuator/health/**` de cada serviço
- `/swagger-ui/**` e `/v3/api-docs/**` de cada serviço

## Requisitos

Para rodar com Docker:

- Docker e Docker Compose
- Git

Não é necessário instalar Java localmente para subir os bancos de dados via Docker.

Para rodar os serviços fora do Docker:

- Java 21
- Git

O projeto possui Maven Wrapper, então não é necessário instalar Maven localmente.

## Estrutura do Monorepo

```text
mecanica/
├── .github/workflows/           # Pipelines de CI/CD
├── docker/                      # Inicialização do PostgreSQL local
├── infra/                       # Cluster e banco provisionados por Terraform
├── k8s/                         # Manifestos Kubernetes e overlays Kustomize
├── postman/                     # Collections por microsserviço e collection completa
├── pom.xml                      # Root POM (packaging pom), dependencyManagement e plugins
└── services/                    # Módulos dos microsserviços (Maven multi-módulo)
    ├── pom.xml                  # POM agregador dos módulos de serviço
    ├── cliente/                 # Microsserviço de Clientes (porta 8081)
    ├── veiculo/                 # Microsserviço de Veículos (porta 8082)
    ├── funcionario/             # Microsserviço de Funcionários (porta 8083)
    ├── servico/                 # Microsserviço de Serviços (porta 8084)
    ├── estoque/                 # Microsserviço de Estoque/Insumos (porta 8085)
    └── atendimento/             # Microsserviço de Ordens de Serviço + Auth/JWT embutido (porta 8086)
```

Cada `services/<nome>` possui, no mínimo:

```text
services/<nome>/
├── pom.xml               # Dependências, testes, JaCoCo do serviço
├── compose.yaml          # PostgreSQL exclusivo do serviço
└── src/
    ├── main/java/...     # controller, service, repository, domain, dto, exception, config, infra
    └── test/java/...     # testes unitários e de integração
```

## Executando um Microsserviço isoladamente

O arquivo `compose.app.yaml` executa a aplicação completa. Para desenvolver ou testar apenas um microsserviço, cada serviço também possui seu próprio `compose.yaml`, responsável pelo PostgreSQL correspondente.

Para subir o banco de dados de um serviço (exemplo com `cliente`):

```bash
docker compose -f services/cliente/compose.yaml up -d
```

Repita o comando trocando `cliente` pelo nome do serviço desejado (`veiculo`, `funcionario`, `servico`, `estoque`, `atendimento`).

Em seguida, execute a aplicação do serviço (Linux/macOS):

```bash
./mvnw -pl services/cliente spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd -pl services/cliente spring-boot:run
```

Ao executar um serviço diretamente pela IDE ou Maven, os valores locais padrão continuam disponíveis. O Compose completo exige `MECANICA_POSTGRES_USER`, `MECANICA_POSTGRES_PASSWORD` e `MECANICA_JWT_SECRET` no `.env` para não manter credenciais fixas no YAML. O prefixo evita colisões com outras stacks do monorepo.

## Subindo toda a aplicação com Docker

Na primeira execução, crie o `.env` local a partir do exemplo:

```powershell
Copy-Item .env.example .env
```

Revise pelo menos `MECANICA_POSTGRES_USER`, `MECANICA_POSTGRES_PASSWORD`, `MECANICA_JWT_SECRET`, `MECANICA_ADMIN_EMAIL` e `MECANICA_ADMIN_PASSWORD`. Os valores do exemplo são exclusivos para desenvolvimento local.

Para construir e iniciar os seis microsserviços e o PostgreSQL de uma vez:

```powershell
docker compose -f compose.app.yaml up -d --build
```

O PostgreSQL desse compose cria um banco independente para cada microsserviço. As APIs ficam disponíveis nas portas `8081` a `8086`, compatíveis com a collection unificada do Postman.

Cada imagem compila somente o microsserviço indicado no argumento `SERVICE`. Os containers são considerados saudáveis quando `GET /actuator/health/readiness` responde com sucesso.

Para acompanhar o estado dos containers:

```powershell
docker compose -f compose.app.yaml ps
```

Para encerrar a aplicação sem apagar os bancos:

```powershell
docker compose -f compose.app.yaml down
```

## Infraestrutura como Código (Terraform)

O módulo [`infra/`](infra/) provisiona a infraestrutura local exigida pela Fase 2. Os manifests da aplicação continuam sob responsabilidade do Kustomize em `k8s/`.

| Arquivo | Recurso criado |
|---|---|
| `infra/cluster.tf` | Cluster Kubernetes local com kind |
| `infra/namespace.tf` | Namespace `mecanica` |
| `infra/database.tf` | Secret e ConfigMap do PostgreSQL, StatefulSet com PVC de 2 Gi e Services |

Pré-requisitos: Docker em execução, Terraform 1.9 ou superior e `kubectl`. Para criar o cluster, namespace, PostgreSQL e os seis bancos lógicos:

```powershell
Set-Location infra
terraform init
terraform apply
Set-Location ..
```

O kind registra o contexto `kind-mecanica` no kubeconfig. Se necessário, exporte-o novamente:

```powershell
kind export kubeconfig --name mecanica
```

O PostgreSQL usa `var.postgres_username` e `var.postgres_password`. Esses valores devem coincidir com `DATABASE_USERNAME` e `DATABASE_PASSWORD` de `k8s/overlays/local/secrets/shared.env`. Os valores dos arquivos `.env.example` já são compatíveis com os defaults locais do Terraform.

Para destruir todo o ambiente local de forma consistente, incluindo cluster e banco:

```powershell
Set-Location infra
terraform destroy
Set-Location ..
```

Não remova o namespace `mecanica` manualmente enquanto ele estiver no estado do Terraform, pois isso também apaga o PostgreSQL e deixa o estado dessincronizado.

## Kubernetes

Os manifestos usam Kustomize e estão organizados assim:

```text
k8s/
├── base/                         # 6 Deployments, Services, ConfigMaps e HPAs
├── overlays/
│   ├── local/                    # Secrets e imagens locais
│   └── production/               # Imagens e banco externos
└── addons/metrics-server/        # Addon aplicado separadamente
```

Configuração principal:

- Services `ClusterIP` nas portas 8081 a 8086;
- init containers aguardando o PostgreSQL;
- probes de startup, liveness e readiness via Spring Boot Actuator;
- shutdown gracioso de 30 segundos no Spring e 45 segundos no Pod;
- HPA `autoscaling/v2`, com CPU em 70%, memória em 75% e máximo de 5 réplicas;
- mínimo de 1 réplica local e 2 em produção;
- Metrics Server v0.9.0.

| Serviços | Requests | Limits |
|---|---|---|
| cliente, veiculo, funcionario, servico e estoque | 100m CPU / 384Mi | 500m CPU / 512Mi |
| atendimento | 200m CPU / 512Mi | 750m CPU / 768Mi |

### Ambiente local

Depois do `terraform apply`, construa as imagens e carregue-as no cluster kind:

```powershell
docker compose -f compose.app.yaml build
kind load docker-image mecanica/cliente:local mecanica/veiculo:local mecanica/funcionario:local mecanica/servico:local mecanica/estoque:local mecanica/atendimento:local --name mecanica
```

Crie os arquivos locais de Secrets, ignorados pelo Git:

```powershell
Copy-Item k8s/overlays/local/secrets/shared.env.example k8s/overlays/local/secrets/shared.env
Copy-Item k8s/overlays/local/secrets/atendimento.env.example k8s/overlays/local/secrets/atendimento.env
Copy-Item k8s/overlays/local/secrets/external-services.env.example k8s/overlays/local/secrets/external-services.env
```

Instale o Metrics Server e aplique a aplicação:

```powershell
kubectl apply -k k8s/addons/metrics-server/overlays/local
kubectl apply -k k8s/overlays/local
kubectl wait --for=condition=available deployment --all -n mecanica --timeout=300s
```

O overlay local habilita `--kubelet-insecure-tls` apenas para o Metrics Server do kind. Essa opção não é aplicada em produção.

Confira os recursos e as métricas:

```powershell
kubectl get pods,services,hpa -n mecanica
kubectl top pods -n mecanica
```

Como os Services são `ClusterIP`, use port-forward para acesso externo. Exemplo:

```powershell
kubectl port-forward service/atendimento 8086:8086 -n mecanica
```

Se o Compose estiver usando as portas 8081 a 8086, encerre-o antes do port-forward. Para a collection completa, encaminhe também os Services das portas 8081 a 8085.

### Secrets

| Secret | Chaves esperadas |
|---|---|
| `mecanica-shared-secrets` | `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET` |
| `atendimento-secrets` | `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD` |
| `external-services-secrets` | `EXTERNAL_SERVICE_TOKEN`, reservado para integrações futuras |

O `JWT_SECRET` deve ser igual nos seis serviços e ter ao menos 32 bytes. Kubernetes Secrets em Base64 não são criptografia; em produção, use um gerenciador de segredos ou External Secrets e nunca versione valores reais.

Trocar `DATABASE_PASSWORD` no Secret não altera a senha de um PostgreSQL já inicializado. Da mesma forma, `ADMIN_PASSWORD` é usado somente ao criar o administrador inicial; rotações precisam ser coordenadas no banco.

### Produção

Antes de aplicar `k8s/overlays/production`:

1. Substitua `postgres.example.internal` pelo DNS real do banco.
2. Configure o issuer JWT, SMTP e endereços de notificação.
3. Use registry e tags imutáveis para as seis imagens.
4. Crie os seis bancos lógicos no PostgreSQL externo.
5. Materialize os Secrets no namespace `mecanica-production` usando o gerenciador de segredos.

```powershell
kubectl apply -k k8s/addons/metrics-server/overlays/production
kubectl apply -k k8s/overlays/production
kubectl wait --for=condition=available deployment --all -n mecanica-production --timeout=300s
```

O overlay de produção não cria o PostgreSQL nem persiste credenciais; o Service `postgres-mecanica` funciona apenas como alias DNS para o banco externo.

### Validação

Sem acessar um cluster, valide a renderização:

```powershell
kubectl kustomize k8s/base
kubectl kustomize k8s/overlays/local
kubectl kustomize k8s/overlays/production
kubectl kustomize k8s/addons/metrics-server/overlays/local
kubectl kustomize k8s/addons/metrics-server/overlays/production
```

Com o cluster ativo, valide os recursos contra a API antes de aplicar:

```powershell
kubectl apply --dry-run=server -k k8s/overlays/local
```

Limitações conhecidas antes de produção real:

- `ddl-auto: update` ainda altera o schema na inicialização e deve ser substituído por migrações versionadas;
- a criação do administrador precisa ser atômica quando houver múltiplas réplicas;
- o HPA de memória de aplicações JVM deve ser calibrado com métricas reais;
- o script SQL local só executa quando o PVC do PostgreSQL está vazio.

## CI/CD

O GitHub Actions automatiza a validação e a implantação:

- [Pull Request Validation](.github/workflows/maven.yml) executa build, testes e verificação de cobertura dos seis microsserviços em pull requests para `main`;
- [Continuous Deployment](.github/workflows/cd.yml) é acionado em pushes para `main` ou manualmente, executa novamente os testes, publica as seis imagens no GHCR, provisiona um cluster kind efêmero e o PostgreSQL com Terraform, aplica os manifestos Kubernetes e valida o rollout.

Durante o deploy, o workflow cria uma credencial temporária de pull para permitir que o cluster baixe imagens privadas do GHCR. O cluster e essa credencial são destruídos ao final da execução, inclusive em caso de falha.

## Configuração do Serviço `atendimento`

O serviço `atendimento` concentra autenticação JWT, notificações por e-mail e integrações HTTP com os demais serviços. As variáveis usadas pelo Compose ficam no `.env`; consulte `.env.example` para os valores locais:

- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`: banco `mecanica_atendimento`.
- `MECANICA_JWT_SECRET` (mínimo 32 caracteres), `MECANICA_JWT_EXPIRATION_SECONDS`, `MECANICA_JWT_ISSUER`: emissão/validação do token JWT no Compose.
- `MECANICA_ADMIN_EMAIL`, `MECANICA_ADMIN_PASSWORD`, `MECANICA_ADMIN_NOME`: admin criado automaticamente na inicialização do serviço pelo Compose.
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`: envio de e-mail.
- `NOTIFICACAO_EMAIL_ADMIN`, `NOTIFICACAO_EMAIL_REMETENTE`: notificações administrativas (`skip` por padrão).
- `CLIENTE_SERVICE_URL`, `VEICULO_SERVICE_URL`, `SERVICO_SERVICE_URL`, `ESTOQUE_SERVICE_URL`: URLs base dos demais serviços (por padrão, `http://localhost:<porta>` de cada um).

## Admin Padrão

Ao iniciar, o serviço `atendimento` cria automaticamente um funcionário administrador caso ele ainda não exista.

Credenciais padrão:

```text
email: admin@mecanica.com
senha: admin123
```

Esses valores podem ser alterados por variáveis de ambiente:

```env
ADMIN_EMAIL=admin@mecanica.com
ADMIN_PASSWORD=admin123
ADMIN_NOME=Administrador Padrão
```

## Login JWT

Faça login em:

```http
POST http://localhost:8086/auth/login
```

Body:

```json
{
  "email": "admin@mecanica.com",
  "senha": "admin123"
}
```

A resposta retorna um token JWT. Use esse token como Bearer Token nos endpoints administrativos:

```http
Authorization: Bearer <token>
```

## Postman

A [collection completa do Postman](postman/mecanica-completa.postman_collection.json) cobre todos os endpoints dos seis microsserviços e já contém as URLs locais, autenticação Bearer e scripts para salvar o token e os IDs criados. As collections separadas por microsserviço também permanecem em `postman/`.

Para usar:

1. Baixe e importe a [collection completa](postman/mecanica-completa.postman_collection.json) ou a collection do serviço desejado.
2. Suba o banco do serviço (`docker compose -f services/<nome>/compose.yaml up -d`) e inicie a aplicação (`./mvnw -pl services/<nome> spring-boot:run`).
3. Na collection unificada, execute primeiro `00 - Autenticação > Login e salvar token` (porta 8086).

O script do request de login salva o token JWT automaticamente na variável `accessToken`. Os cadastros também atualizam automaticamente `clienteId`, `veiculoId`, `funcionarioId`, `servicoId` e `insumoId` para uso nas requisições seguintes.

## Testes

Para rodar os testes de um serviço isoladamente (Linux/macOS):

```bash
./mvnw -pl services/cliente -am verify
```

No Windows:

```powershell
.\mvnw.cmd -pl services/cliente -am verify
```

Para rodar os testes de todos os serviços de uma vez:

```bash
./mvnw -pl services/cliente,services/veiculo,services/funcionario,services/servico,services/estoque,services/atendimento -am verify
```

Os testes usam profile de teste com H2 em memória, então não dependem do PostgreSQL local. A cobertura mínima obrigatória é de 80% de linhas e branches por serviço, validada via JaCoCo (`jacoco-check`).

Para gerar relatório de cobertura JaCoCo de um serviço:

```bash
./mvnw -pl services/cliente test jacoco:report
```

O relatório fica em:

```text
services/cliente/target/site/jacoco/index.html
```

## Parar e Limpar Docker

Parar toda a aplicação sem apagar o banco:

```powershell
docker compose -f compose.app.yaml down
```

Limpar completamente a aplicação, incluindo o volume do PostgreSQL e as imagens locais:

```powershell
docker compose -f compose.app.yaml down -v --rmi local --remove-orphans
```

O segundo comando apaga definitivamente os dados locais dos seis bancos. Na próxima subida, `docker/postgres/init-databases.sql` será executado novamente.

Parar o banco de um serviço:

```bash
docker compose -f services/cliente/compose.yaml down
```

Use comandos com `-v` apenas quando quiser descartar os dados persistidos localmente.

## Problemas Comuns

Se um serviço com autenticação (`atendimento`) falhar por causa do JWT:

- confira se `JWT_SECRET` está definido no ambiente onde o serviço roda;
- confira se a secret tem pelo menos 32 caracteres.

Se a porta estiver ocupada:

- `cliente` usa `8081`, `veiculo` usa `8082`, `funcionario` usa `8083`, `servico` usa `8084`, `estoque` usa `8085`, `atendimento` usa `8086`;
- os bancos PostgreSQL de cada serviço usam `5432` (`cliente`), `5433` (`veiculo`), `5434` (`funcionario`), `5435` (`servico`), `5436` (`estoque`) e `5437` (`atendimento`);
- pare outros serviços nessas portas ou ajuste o `compose.yaml`/variáveis de ambiente do serviço correspondente.

Se o login falhar após limpar volumes:

- aguarde o serviço `atendimento` terminar de subir;
- confira nos logs se o admin padrão foi criado;
- use as credenciais padrão ou as variáveis `ADMIN_EMAIL` e `ADMIN_PASSWORD` configuradas.
