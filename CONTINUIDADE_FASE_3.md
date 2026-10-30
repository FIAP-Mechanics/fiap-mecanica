# Passagem de trabalho — deploy e entrega da Fase 3

Atualizado em **30/10/2026**. Fonte dos requisitos: [`13SOAT - Fase 3 - Tech Challenge.pdf`](13SOAT%20-%20Fase%203%20-%20Tech%20Challenge.pdf), páginas 2 a 5. Este arquivo é um roteiro para quem vai concluir o projeto; os registros do GitHub citados abaixo são evidências históricas, não uma confirmação de que a AWS esteja ativa agora.

## Situação encontrada

- A conta AWS Academy usada anteriormente excedeu os US$ 50 de créditos e foi desativada, conforme o painel e o e-mail apresentados pelo titular. **Não tentar novo deploy nela antes de obter reativação/aumento de orçamento ou outra conta autorizada.** A [FAQ da Vocareum](https://help.vocareum.com/en/articles/3705765-faq) orienta contatar o instrutor ou o suporte AWS Academy quando o orçamento do lab acaba. O estado dos recursos antigos precisa ser conferido por alguém com acesso à conta; encerrar o lab não é evidência suficiente de que cada recurso foi removido.
- Os quatro repositórios têm código e workflows de CI/deploy. O run de **produção da aplicação** [40796409298](https://github.com/FIAP-Mechanics/fiap-mecanica/actions/runs/40796409298) terminou com build, aplicação dos manifests e smoke test aprovados em 29/10. Isso não comprova que os três repositórios de Terraform/Lambda implantam por seus próprios pipelines nem que o endpoint continua disponível.
- Os últimos runs de **homologação** consultados em 30/10 falharam: [EKS](https://github.com/FIAP-Mechanics/fiap-mecanica-k8s-infra/actions/runs/40799828694), [RDS](https://github.com/FIAP-Mechanics/fiap-mecanica-infra-dbs/actions/runs/40799830484), [Lambda](https://github.com/FIAP-Mechanics/fiap-mecanica-lambda/actions/runs/40799826726) e [aplicação](https://github.com/FIAP-Mechanics/fiap-mecanica/actions/runs/40799825203). Os três primeiros mostraram `terraform init`: `The value cannot be empty or all whitespace` para o bucket S3; os logs também mostraram `AWS_*` vazios naquele run. A aplicação parou em `Check required Environment secrets and variables`. As variables/secrets podem ter sido alteradas depois: verificar o estado atual antes de corrigir.
- Os quatro PRs de documentação continuam abertos, com CI aprovado: [aplicação #47](https://github.com/FIAP-Mechanics/fiap-mecanica/pull/47), [EKS #1](https://github.com/FIAP-Mechanics/fiap-mecanica-k8s-infra/pull/1), [RDS #1](https://github.com/FIAP-Mechanics/fiap-mecanica-infra-dbs/pull/1) e [Lambda #1](https://github.com/FIAP-Mechanics/fiap-mecanica-lambda/pull/1). Mesclar o #47 primeiro, pois os outros READMEs apontam para o guia central nele. Fazer merge pelos PRs, seguindo as revisões exigidas.
- Na última inspeção, `main` e `homologacao` tinham regras de proteção nos quatro repositórios, os Environments `producao` e `homologacao` existiam, e `soat-architecture` aparecia como colaborador. Reconfirmar antes da entrega, pois essas configurações podem mudar.

## Dependências e ordem dos quatro deploys

| Ordem | Repositório / workflow | O que entrega | State S3 |
|---|---|---|---|
| 1 | [fiap-mecanica-k8s-infra](https://github.com/FIAP-Mechanics/fiap-mecanica-k8s-infra), `.github/workflows/deploy.yml` | VPC, subnets, NAT, EKS e node group; opcionalmente dashboards/alertas do New Relic | `infra-k8s/<ambiente>.tfstate`, `observability/<ambiente>.tfstate` |
| 2 | [fiap-mecanica-infra-dbs](https://github.com/FIAP-Mechanics/fiap-mecanica-infra-dbs), `.github/workflows/deploy.yml` | RDS PostgreSQL privado na VPC criada no passo 1 | `infra-db/<ambiente>.tfstate` |
| 3 | [fiap-mecanica-lambda](https://github.com/FIAP-Mechanics/fiap-mecanica-lambda), `.github/workflows/deploy.yml` | ECR, Lambda `auth-cpf`, Secrets Manager e acesso ao RDS | `auth-cpf/<ambiente>.tfstate` |
| 4 | [fiap-mecanica](https://github.com/FIAP-Mechanics/fiap-mecanica), `.github/workflows/deploy-aws.yml` | Seis imagens no GHCR; Jobs de inicialização do banco, Kong e seis serviços no EKS; smoke test | Lê os states 1 e 2, não cria state AWS próprio |

O `TF_STATE_BUCKET` pode ser o mesmo **dentro da mesma conta AWS** para os dois ambientes, porque as chaves acima são separadas. Os Environments do GitHub pertencem a cada repositório: colocar um secret no repo da aplicação não o disponibiliza aos outros três. O fluxo detalhado está em [`ARQUITETURA.md`](ARQUITETURA.md) e no [README da aplicação](README.md#deploy-na-aws-aws-academy).

## 1. Conseguir uma conta AWS utilizável

1. Usar um Learner Lab reativado com orçamento suficiente, ou outra conta autorizada para EKS, RDS, Lambda, ECR, S3, VPC e balanceador. Conferir o limite disponível **antes** de criar EKS/NAT/RDS. A conta anterior está desativada segundo o titular.
2. Se for **a mesma conta** e os recursos/states ainda existirem, verificar o estado antes de aplicar Terraform novamente. Preservar `DATABASE_PASSWORD`, `AUTH_CPF_DB_PASSWORD` e `JWT_SECRET` existentes; trocar esses valores sem coordenação pode quebrar o RDS, a Lambda, o Kong e os tokens.
3. Se for **outra conta**, tratar como implantação nova. O bucket `mecanica-tfstate-733005772849` é da conta anterior; não apontar os novos workflows para ele. Criar o bucket na nova conta pelo `scripts/aws-lab-bootstrap.sh` do repositório de EKS (ou procedimento equivalente) e cadastrar o **novo nome** em `TF_STATE_BUCKET` nos quatro repositórios. Confirmar que os `envs/<ambiente>.tfvars`, região, `LabRole` e cotas são compatíveis com a nova conta. As credenciais da conta antiga não migram os recursos nem o state automaticamente.
4. Na máquina de quem executará, validar a sessão com `aws sts get-caller-identity`. Não copiar credenciais para o repositório ou para `.secrets`. O GitHub Actions lê apenas os secrets/variables cadastrados no GitHub.

## 2. Preparar o GitHub em cada repositório

Abrir **Settings → Environments → homologacao** ou **producao** de **cada** repo. Configurar `TF_STATE_BUCKET` em **Environment variables** e os itens abaixo em **Environment secrets**. A pessoa precisa de permissão de administração no respectivo repositório para editar esses valores; a [documentação do GitHub](https://docs.github.com/en/actions/how-tos/deploy/configure-and-manage-deployments/manage-environments) explica Environments e revisores.

| Repositório | Secrets obrigatórios por ambiente |
|---|---|
| EKS | `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` |
| RDS | Os três `AWS_*` + `DATABASE_PASSWORD` |
| Lambda | Os três `AWS_*` + `AUTH_CPF_DB_PASSWORD`, `JWT_SECRET` |
| Aplicação | Os três `AWS_*` + `DATABASE_PASSWORD`, `AUTH_CPF_DB_PASSWORD`, `JWT_SECRET`, `ADMIN_PASSWORD` |

- Usar os três `AWS_*` **da mesma sessão/conta** em todos os repositórios e ambientes que serão implantados. Em Learner Lab as credenciais são temporárias: atualizá-las após cada renovação e iniciar **novo run**. Não enviar os valores por chat ou commit.
- Para o **mesmo ambiente**, `DATABASE_PASSWORD` deve coincidir em RDS e aplicação; `AUTH_CPF_DB_PASSWORD` e `JWT_SECRET`, em Lambda e aplicação. Homologação e produção podem ter conjuntos próprios.
- `AWS_REGION` é opcional nos três repos de infraestrutura/Lambda, com padrão `us-east-1`; a aplicação fixa `us-east-1`. Usar a mesma região em tudo.
- Para observabilidade real, adicionar ao Environment de **EKS** `NEW_RELIC_API_KEY`, `NEW_RELIC_ACCOUNT_ID` e a variable `ALERT_EMAIL`; no Environment da **aplicação**, `NEW_RELIC_LICENSE_KEY`. Sem as chaves, o deploy pode passar, mas a exportação/instalação do coletor e os dashboards/alertas ficam desativados ou ignorados.
- O script `scripts/aws-lab-secrets.sh <ambiente>` do repo EKS pode preencher as quatro configurações usando `aws` e `gh`. **Não executá-lo de forma completa numa conta já implantada sem verificar as senhas:** quando não recebe valores, ele gera novas senhas. Para renovar somente os `AWS_*`, usar `RENOVAR_APENAS_CREDENCIAIS=1` conforme o README; conferir antes qual conta está ativa.

## 3. Rodar e corrigir os workflows

1. Mesclar o código necessário por PR. Para cada ambiente, iniciar o deploy pelo push/merge na branch `homologacao` ou `main`; também há `workflow_dispatch` na aba **Actions**. Selecionar explicitamente a branch correta. `main` seleciona o Environment `producao` e pode aguardar aprovação; `homologacao` seleciona `homologacao`.
2. Rodar **um repositório por vez**, na ordem da tabela, aguardando sucesso antes do próximo. Os workflows não coordenam as dependências entre repositórios; disparar os quatro juntos pode fazer RDS/Lambda/aplicação lerem outputs que ainda não existem. O CI verde do PR verifica código, mas não substitui um run de deploy bem-sucedido.
3. No EKS, conferir `eks` aprovado e `cluster_name` no state; se a observabilidade for exigida, conferir também o job `observability`. No RDS, conferir `rds` aprovado e `db_host`. Na Lambda, conferir `deploy` aprovado (não apenas `publish-image`) e o smoke test da invocação. Na aplicação, conferir `build-and-push`, `deploy`, rollout dos sete Deployments, Jobs `rds-bootstrap`/`auth-cpf-db-init` e `Smoke test`.
4. Falha `terraform init` com bucket vazio: conferir `TF_STATE_BUCKET` no **Environment do repo e da branch do run** e iniciar novo run. Falha `ExpiredTokenException`: renovar os três `AWS_*`. Falha por output inexistente: confirmar state/bucket/ambiente e o deploy anterior. Falha `Pending` ou timeout no EKS: verificar node group `Ready`, capacidade dos nós, pods e eventos do namespace; os diagnósticos aparecem no job da aplicação.
5. Guardar os links dos **runs bem-sucedidos de homologação e produção nos quatro repos**, além dos endereços de serviço. O PDF exige deploy automático das duas branches. Um sucesso de produção da aplicação em 29/10 não substitui esses oito resultados.

## 4. Validar o sistema publicado

- Obter o hostname público do Load Balancer do `Service/kong` no namespace `mecanica-homologacao` ou `mecanica-production`; o workflow da aplicação o imprime no passo `Smoke test`. O Kong não fornece interface de testes como Swagger; usar a [collection Postman](postman/mecanica-completa.postman_collection.json) ou `scripts/smoke-test.sh` pelo hostname do gateway.
- Testar `POST /auth/cpf` com CPF de cliente ativo; conferir emissão de JWT e acesso às rotas do próprio cliente. Testar CPF inválido/inexistente/inativo, token ausente/inválido e acesso a dados de outro cliente. Confirmar que `GET /cliente` e `GET /cliente/{id}` não expõem CPF/e-mail/telefone/endereço sem autorização, conforme a correção feita no projeto. Registrar status e evidências, sem publicar dados reais.
- Validar abertura de OS e transições com cadastros de teste. `scripts/validate-gateway.mjs` faz fluxo mais completo, mas cria dados QA e deixa a OS no histórico; usar somente no ambiente que admite esse efeito. O smoke do pipeline é mais curto.
- Com New Relic configurado, produzir tráfego e comprovar **dados chegando**: latência, CPU/memória, health/uptime, alerta de falha de OS, logs JSON com `request_id`/`trace.id`, traces, volume diário de OS, tempo médio por status e erros de integração. Código e dashboards declarados não provam observabilidade ativa.
- Colocar nos READMEs os links reais dos deploys ativos e do dashboard. Se o ambiente for desmontado para poupar créditos, indicar isso claramente na documentação e guardar as evidências de execução; o enunciado pede links ativos quando aplicável.

## 5. Fechar os entregáveis do PDF

| Requisito | O que ainda precisa ser feito ou comprovado |
|---|---|
| Quatro repos, CI/CD e governança | Mesclar os quatro PRs de documentação; obter runs de deploy aprovados nas duas branches, com a proteção de `main`, PR obrigatório e acesso de `soat-architecture` reconfirmados. Conferir Dockerfile quando aplicável, READMEs com propósito, tecnologias, execução/deploy, diagrama específico, Swagger/Postman e links reais. |
| API Gateway, CPF, RDS e Kubernetes escalável | Código e Terraform existem; comprovar novamente por deploy independente de cada repo e testes externos via Kong, incluindo node group, HPA, Lambda e RDS. Registrar evidências das rotas protegidas e da correção de exposição de clientes. |
| Observabilidade | Ativar as chaves do New Relic em pelo menos um ambiente, concluir o job `observability` e a instalação do coletor, verificar dashboards/alertas, logs e traces **com dados reais**. O último deploy de produção da aplicação pulou os passos do coletor por ausência da License Key. |
| Arquitetura e decisões | O [`ARQUITETURA.md`](ARQUITETURA.md) já desenha nuvem/APIs/banco/monitoramento; o README tem a sequência de autenticação. Falta publicar **sequência da abertura de OS**, **justificativa formal do PostgreSQL**, **ajustes do modelo relacional** e **diagramas ER** com relacionamentos. Revisar e publicar RFCs/ADRs relevantes (nuvem, banco, autenticação, comunicação/HPA). Há rascunhos em `C:\Users\guilh\Desktop\docs\adr` e `...\rfc`, mas `docs/` está no `.gitignore` deste repo; copiar/revisar para um caminho versionado e corrigir textos antigos que ainda descrevem monorepo ou AWS não implantada. |
| Vídeo | Gravar no YouTube/Vimeo, público ou não listado, **até 15 min**. Mostrar CPF, CI/CD, deploy automático, API protegida, dashboard ao vivo, logs e traces em execução. Guardar o link. |
| PDF final | Enviar **um PDF** no Portal do Aluno com links dos quatro repositórios, vídeo, documentações e confirmação de `soat-architecture` em todos. Conferir que todos os links abrem para o avaliador. |

O PDF apresenta notificações serverless no contexto do desafio, mas a lista de **requisitos obrigatórios** especifica a Function serverless de autenticação por CPF. Não marcar uma segunda Function para notificações como pendência obrigatória sem orientação adicional da disciplina.

## Custo e encerramento

EKS, nós EC2, NAT, RDS e balanceador podem consumir rapidamente o orçamento. Implantar somente quando houver tempo para validar e registrar evidências. No repo EKS, `scripts/aws-lab-down.sh <homologacao|producao>` descreve a desmontagem em ordem inversa e **mantém o bucket de state**; exige credenciais válidas e acesso aos quatro repos lado a lado. Verificar antes o banco e os dados: a configuração de RDS pode excluí-lo sem snapshot final. Depois, conferir os recursos remanescentes e o consumo no console; não assumir que `End Lab` ou `Reset` substitui essa verificação. A [Vocareum](https://help.vocareum.com/en/articles/3705765-faq) informa que valores podem aparecer com atraso no painel.
