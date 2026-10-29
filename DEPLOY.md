# Guia de deploy na AWS Academy

Este guia cobre os quatro repositórios da oficina mecânica:

1. [`fiap-mecanica-k8s-infra`](https://github.com/FIAP-Mechanics/fiap-mecanica-k8s-infra): VPC, EKS e observabilidade.
2. [`fiap-mecanica-infra-dbs`](https://github.com/FIAP-Mechanics/fiap-mecanica-infra-dbs): RDS PostgreSQL.
3. [`fiap-mecanica-lambda`](https://github.com/FIAP-Mechanics/fiap-mecanica-lambda): ECR, Lambda de autenticação e Secrets Manager.
4. [`fiap-mecanica`](https://github.com/FIAP-Mechanics/fiap-mecanica): imagens dos serviços, manifests Kubernetes e Kong.

## Ambientes e isolamento

`homologacao` e `producao` são ambientes AWS separados. Cada um tem sua VPC, seu EKS, seu RDS, sua Lambda e seu Kong. O mesmo bucket S3 pode guardar os states Terraform, pois cada componente usa uma chave diferente: `infra-k8s/<ambiente>.tfstate`, `observability/<ambiente>.tfstate`, `infra-db/<ambiente>.tfstate` e `auth-cpf/<ambiente>.tfstate`. A existência da branch `homologacao` não significa que a infraestrutura correspondente já esteja implantada.

No GitHub, cada repositório tem Environments `homologacao` e `producao`. Um workflow iniciado em `homologacao` usa apenas os secrets e variables desse Environment; um iniciado em `main` usa os de `producao`. O deploy de produção aguarda a aprovação de um dos membros configurados da organização. Os workflows também podem ser iniciados em **Actions > Deploy > Run workflow**, escolhendo a branch correspondente.

## Pré-requisitos

- Learner Lab ativo e credenciais temporárias válidas da sessão atual.
- Bucket S3 para o state Terraform, criado uma vez com `scripts/aws-lab-bootstrap.sh` do repositório `fiap-mecanica-k8s-infra` ou conferido no console AWS. Seu nome deve ser o valor de `TF_STATE_BUCKET` nos quatro repositórios.
- Acesso aos quatro repositórios no GitHub para configurar **Settings > Environments**. Cada repositório possui os próprios secrets, mesmo quando o valor é igual.
- Uma fonte segura para guardar as senhas persistentes do projeto. Secrets do GitHub não podem ser recuperados em texto depois de gravados.

O workflow usa os secrets do GitHub e não lê credenciais da máquina de quem inicia o deploy. Iniciar o Learner Lab, por si só, não atualiza os secrets do Actions.

## Secrets e variables por repositório

Cadastre os valores em **Settings > Environments > homologacao** ou **producao** do repositório indicado. `TF_STATE_BUCKET` é uma **Environment variable**, não um secret. `AWS_REGION` é opcional: os workflows usam `us-east-1` por padrão. Mantenha essa região, pois o workflow da aplicação também usa `us-east-1`. O `GITHUB_TOKEN` é fornecido pelo Actions.

| Repositório | Secrets obrigatórios em cada Environment | Variables |
|---|---|---|
| `fiap-mecanica-k8s-infra` | `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` | `TF_STATE_BUCKET`; `AWS_REGION` opcional |
| `fiap-mecanica-infra-dbs` | Os três `AWS_*` acima e `DATABASE_PASSWORD` | `TF_STATE_BUCKET`; `AWS_REGION` opcional |
| `fiap-mecanica-lambda` | Os três `AWS_*` acima, `AUTH_CPF_DB_PASSWORD` e `JWT_SECRET` | `TF_STATE_BUCKET`; `AWS_REGION` opcional |
| `fiap-mecanica` | Os três `AWS_*` acima, `DATABASE_PASSWORD`, `AUTH_CPF_DB_PASSWORD`, `JWT_SECRET` e `ADMIN_PASSWORD` | `TF_STATE_BUCKET` |

As credenciais `AWS_*` são da **mesma sessão ativa** do Learner Lab nos quatro repositórios. Renove os três valores em cada Environment a cada reinício ou expiração da sessão.

Dentro de **um mesmo ambiente**, mantenha `DATABASE_PASSWORD` igual em `fiap-mecanica-infra-dbs` e `fiap-mecanica`; `AUTH_CPF_DB_PASSWORD` e `JWT_SECRET` iguais em `fiap-mecanica-lambda` e `fiap-mecanica`. Homologação pode ter valores diferentes dos de produção. `ADMIN_PASSWORD` é usado pela aplicação no cadastro inicial do administrador.

Na primeira implantação de homologação, gere uma vez senhas fortes e independentes das de produção e guarde-as em um gerenciador de senhas. Em produção já existente, reutilize os valores atuais: gerar novos valores apenas em um repositório pode quebrar acesso ao banco ou validação dos JWTs. Se os valores anteriores não estiverem disponíveis, planeje uma rotação coordenada antes de executar os workflows de produção.

Observabilidade é opcional para o deploy de infraestrutura: `NEW_RELIC_API_KEY` e `NEW_RELIC_ACCOUNT_ID` vão no Environment de `fiap-mecanica-k8s-infra`, e `NEW_RELIC_LICENSE_KEY` no de `fiap-mecanica`. `ALERT_EMAIL` é uma variable opcional em `fiap-mecanica-k8s-infra`.

### Conferência no GitHub

Abra, em cada repositório, **Settings > Environments > nome do ambiente** e confira os **nomes** dos secrets e variables da tabela. O GitHub mostra a existência e a data da última atualização, mas não o conteúdo dos secrets. Confira também se o Environment `producao` aceita apenas `main` e tem revisores; `homologacao` aceita apenas `homologacao`.

O script [`aws-lab-secrets.sh`](https://github.com/FIAP-Mechanics/fiap-mecanica-k8s-infra/blob/main/scripts/aws-lab-secrets.sh) pode cadastrar os valores a partir do perfil AWS ativo, usando `aws`, `gh`, Bash e `openssl`. Sua execução completa gera novas senhas quando as variáveis correspondentes não estão definidas. Para renovar **somente** as credenciais temporárias de uma sessão, use `RENOVAR_APENAS_CREDENCIAIS=1`; preserve as senhas persistentes. A configuração pela interface do GitHub dispensa essas ferramentas locais.

## Executar o deploy

Para **cada ambiente**, execute os workflows nesta ordem e espere cada um terminar com sucesso antes de iniciar o próximo:

1. [Kubernetes: Deploy](https://github.com/FIAP-Mechanics/fiap-mecanica-k8s-infra/actions/workflows/deploy.yml) — cria rede e EKS; aplica observabilidade se as credenciais do New Relic existirem.
2. [Banco: Deploy](https://github.com/FIAP-Mechanics/fiap-mecanica-infra-dbs/actions/workflows/deploy.yml) — cria o RDS na rede do passo anterior.
3. [Lambda: Deploy](https://github.com/FIAP-Mechanics/fiap-mecanica-lambda/actions/workflows/deploy.yml) — publica a imagem e cria a função de autenticação.
4. [Aplicação: Deploy AWS](https://github.com/FIAP-Mechanics/fiap-mecanica/actions/workflows/deploy-aws.yml) — publica os serviços, instala Kong e executa o smoke test.

Em **Run workflow**, selecione `homologacao` para homologação ou `main` para produção. Um merge por PR nessas branches também dispara o deploy correspondente. Em produção, um dos revisores do Environment precisa liberar o job quando o Actions solicitar. Os repositórios de banco, Lambda e aplicação leem outputs dos states criados nos passos anteriores; por isso a ordem importa.

Após o quarto workflow, copie o hostname do LoadBalancer do Kong informado no job `deploy`. Acesse uma rota válida, por exemplo `GET /cliente` com autenticação, ou configure as variáveis de URL da [collection Postman](postman/mecanica-completa.postman_collection.json) com esse hostname. O script [`scripts/smoke-test.sh`](scripts/smoke-test.sh) também valida o gateway. Abrir somente a raiz `/` pode retornar `no Route matched`, porque ela não é uma rota da API.

## Diagnóstico dos erros já encontrados

- `terraform init` com `Invalid Value` no backend S3 e `The value cannot be empty`: falta `TF_STATE_BUCKET` no Environment usado pelo run. A variável foi cadastrada nos oito Environments após a primeira execução de homologação; inicie um **novo** run para usar a configuração atual.
- `Check required Environment secrets and variables`: um secret obrigatório da tabela está ausente no Environment selecionado. O run de homologação da aplicação parou nessa checagem.
- `ExpiredTokenException` ou `InvalidClientTokenId`: atualize os três secrets `AWS_*` da sessão ativa nos quatro repositórios e no ambiente correto.
- `AccessDenied`: confira a sessão do Learner Lab e as permissões da `LabRole`; os workflows não criam usuários ou roles IAM.
- EKS, RDS ou Lambda sem output esperado: confira se o workflow anterior terminou com sucesso e se todos usam o mesmo bucket com chaves de state correspondentes ao ambiente.

Em 28/10/2026, os quatro primeiros runs de `homologacao` foram acionados, mas falharam antes do deploy AWS por configuração ausente. A variável `TF_STATE_BUCKET` foi adicionada depois desses runs. Ainda é necessário cadastrar os secrets de homologação nos quatro repositórios e os de produção nos três repositórios de infraestrutura/Lambda, então executar novamente os workflows na ordem acima. Consulte os runs: [Kubernetes](https://github.com/FIAP-Mechanics/fiap-mecanica-k8s-infra/actions/runs/40799828694), [Banco](https://github.com/FIAP-Mechanics/fiap-mecanica-infra-dbs/actions/runs/40799830484), [Lambda](https://github.com/FIAP-Mechanics/fiap-mecanica-lambda/actions/runs/40799826726) e [Aplicação](https://github.com/FIAP-Mechanics/fiap-mecanica/actions/runs/40799825203).
