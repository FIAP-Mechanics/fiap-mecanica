# Infraestrutura (Terraform)

Provisiona, via Terraform, o que o PDF da Fase 2 pede explicitamente na seção de IaC: o **cluster
Kubernetes** (local, via `kind`) e o **banco de dados** (PostgreSQL). Os Deployments/Services/HPA da
aplicação continuam sendo aplicados separadamente com `kubectl apply -k` sobre `/k8s` — o Terraform
não conhece os 6 microsserviços, só a infraestrutura de base sobre a qual eles rodam.

## Recursos criados

| Arquivo | Recurso Terraform | O que é |
|---|---|---|
| `cluster.tf` | `kind_cluster.this` | Cluster Kubernetes local (kind) |
| `namespace.tf` | `kubernetes_namespace_v1.mecanica` | Namespace `mecanica` (mesmo nome usado em `k8s/base/namespace.yaml`) |
| `database.tf` | `kubernetes_secret_v1.postgres_credentials` | Credenciais do PostgreSQL (usuário/senha) |
| `database.tf` | `kubernetes_config_map_v1.postgres_init_databases` | Script de criação dos 6 bancos lógicos, lido de `../docker/postgres/init-databases.sql` |
| `database.tf` | `kubernetes_stateful_set_v1.postgres_mecanica` | PostgreSQL 16 com PVC de 2Gi |
| `database.tf` | `kubernetes_service_v1.postgres_mecanica` (+ `_headless`) | Service ClusterIP `postgres-mecanica`, mesmo nome referenciado em `SPRING_DATASOURCE_URL` nos ConfigMaps de `k8s/base` |

O Metrics Server **não** está aqui: ele não é citado no requisito de IaC do PDF (só cluster + banco),
e já existe como um addon Kustomize próprio em `k8s/addons/metrics-server`, aplicado via `kubectl`.

## Pré-requisitos

- Docker rodando.
- `terraform` >= 1.9.
- `kubectl`.

## Como aplicar

```bash
cd infra
terraform init
terraform apply
```

O `kind` já grava o kubeconfig do cluster em `~/.kube/config` (contexto `kind-mecanica`). Se preferir
extrair explicitamente:

```bash
kind export kubeconfig --name mecanica
```

Depois de o Terraform terminar (cluster + namespace + Postgres no ar), aplique a aplicação:

```bash
docker compose -f ../compose.app.yaml build
kubectl apply -k ../k8s/addons/metrics-server/overlays/local
kubectl apply -k ../k8s/overlays/local
kubectl wait --for=condition=available deployment --all -n mecanica --timeout=300s
```

> As imagens `mecanica/<serviço>:local` precisam existir no cluster kind antes do `kubectl apply`.
> Como o kind não enxerga o cache de imagens do host, carregue cada uma com
> `kind load docker-image mecanica/<serviço>:local --name mecanica` depois do build.

## Credenciais do banco

`database.tf` usa `var.postgres_username`/`var.postgres_password` (default: `mecanica` /
`mecanica-local-only`, os mesmos valores de `k8s/overlays/local/secrets/shared.env.example`) para
configurar o PostgreSQL. Os Pods da aplicação, por sua vez, recebem `DATABASE_USERNAME`/
`DATABASE_PASSWORD` de um Secret **diferente** (`mecanica-shared-secrets`), gerado pelo Kustomize a
partir de `k8s/overlays/local/secrets/shared.env` — de propósito, para não haver dois sistemas
(Terraform e Kustomize) gerenciando o mesmo objeto `Secret`.

**Isso significa que os dois arquivos precisam ter as mesmas credenciais.** Se você copiar
`shared.env.example` para `shared.env` sem editar (fluxo padrão local, documentado em
`k8s/README.md`), já bate com o default deste módulo — nenhuma ação extra é necessária. Se você
trocar a senha em um dos dois lugares, troque no outro também (ex.: via `-var` ou `terraform.tfvars`,
já ignorado pelo Git).

## Destruir

```bash
kubectl delete -k ../k8s/overlays/local
kubectl delete -k ../k8s/addons/metrics-server/overlays/local
cd infra && terraform destroy
```

A ordem importa: destruir o cluster kind antes de remover os manifestos da aplicação apenas apaga o
cluster inteiro de qualquer forma (o `kind_cluster` é o nó Docker), mas destruir nessa ordem evita
qualquer `Terminating` pendurado por finalizers de PVC.

## Portabilidade para cloud (fora do escopo desta fase)

Só `cluster.tf` mudaria numa migração para AWS: trocar `provider "kind"` +
`resource "kind_cluster"` por `hashicorp/aws` + um módulo de EKS. `namespace.tf` e `database.tf`
continuariam válidos como estão (usam apenas o provider `kubernetes`, agnóstico de onde o cluster
roda) — só `var.postgres_storage_class` precisaria apontar para uma StorageClass de EBS em vez da
padrão do kind. Fora do Terraform, uma migração real para AWS também trocaria o registro de imagens
(ECR), a exposição externa (ALB Ingress em vez de `kubectl port-forward`) e, idealmente, o próprio
PostgreSQL por uma instância RDS gerenciada em vez do StatefulSet — mas isso é uma fase futura, fora
do escopo deste Tech Challenge.
