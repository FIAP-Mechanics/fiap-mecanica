# Oficina Mecânica

Monorepo de microsserviços para gerenciamento de uma oficina mecânica. O sistema cobre cadastro de clientes, veículos, serviços, estoque/insumos, abertura e acompanhamento de ordens de serviço, cálculo de orçamento, notificações, autenticação JWT e controle de permissões por perfil.

O projeto está organizado como um monorepo Maven multi-módulo, sem API Gateway: cada serviço é acessado diretamente pela sua própria porta. Consulte `docs/migracao-microsservicos.md` para o histórico completo da migração do monólito original para microsserviços.

## Objetivo

O objetivo do projeto é apoiar o fluxo principal de atendimento de uma oficina, desde a chegada do cliente até a entrega do veículo, mantendo rastreabilidade da ordem de serviço e separando as responsabilidades entre atendente, mecânico, administrador e cliente.

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
8. O cliente acompanha a OS por `GET /atendimento/{id}` e decide a aprovação em comunicação manual com a atendente.
9. Se aprovado, a atendente registra em `POST /atendimento/{id}/aprovar`; o sistema baixa os insumos do estoque (`services/estoque`, porta 8085) e muda a OS para `EM_EXECUCAO`.
10. Se recusado, o cancelamento é registrado em `POST /atendimento/{id}/cancelar`.
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

As notificações usam templates cadastrados no serviço `atendimento`.

- Cliente: recebe aviso para autorização de orçamento, retirada do veículo e confirmação de veículo retirado.
- Funcionários: recebem aviso quando o estoque não possui quantidade suficiente e há necessidade de reposição.

As configurações de e-mail do serviço `atendimento` são lidas das variáveis de ambiente `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `NOTIFICACAO_EMAIL_ADMIN` e `NOTIFICACAO_EMAIL_REMETENTE`. Por padrão, `NOTIFICACAO_EMAIL_ADMIN` e `NOTIFICACAO_EMAIL_REMETENTE` já vêm como `skip`, o que desabilita o envio real de e-mails. Quando o envio está em `skip`, o sistema não busca templates de notificação.

Para habilitar envio real, configure um remetente válido nessas variáveis e mantenha os templates cadastrados no banco.

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
- `/swagger-ui/**` e `/v3/api-docs/**` de cada serviço

> **Pendência conhecida:** em `cliente`, `veiculo` e `estoque`, a anotação `@Secured` em nível de classe também bloqueia os endpoints `GET`, que deveriam ser públicos. Consulte `docs/migracao-microsservicos.md` para detalhes.

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
├── pom.xml                      # Root POM (packaging pom), apenas dependencyManagement/properties
├── compose.yaml                 # Stack local do Dependency-Track (profile "security")
├── docs/                        # Documentação (plano de migração, diagramas, OpenAPI)
├── postman/                     # Collections e environments por microsserviço
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

## Subindo um Microsserviço

Não existe mais um `docker compose up` único para toda a aplicação. Cada serviço tem seu próprio `compose.yaml`, com o PostgreSQL correspondente, e é executado individualmente.

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

Os serviços já possuem valores padrão sensatos (usuário/senha `mecanica`, portas de banco e de aplicação descritas na tabela acima), então normalmente não é necessário nenhum arquivo `.env` para rodar localmente. Cada serviço permite sobrescrever suas configurações por variáveis de ambiente (ex.: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`), caso necessário.

Para subir todos os 6 serviços de uma vez (bancos de dados), execute o comando acima para cada `services/<nome>/compose.yaml` e depois inicie cada aplicação em um terminal separado (ou via IDE), na ordem que preferir — não há dependência de inicialização entre eles, exceto que `atendimento` chama os demais via HTTP em tempo de execução (portanto, para o fluxo completo de OS, os 5 serviços de domínio devem estar de pé antes de usar `atendimento`).

Verifique se um container de banco está rodando:

```bash
docker compose -f services/cliente/compose.yaml ps
```

## Configuração do Serviço `atendimento`

O serviço `atendimento` concentra autenticação JWT, notificações por e-mail e integrações HTTP com os demais serviços. Principais variáveis de ambiente (todas com valor padrão para uso local):

- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`: banco `mecanica_atendimento`.
- `JWT_SECRET` (mínimo 32 caracteres), `JWT_EXPIRATION_SECONDS`, `JWT_ISSUER`: emissão/validação do token JWT.
- `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NOME`: admin criado automaticamente na inicialização do serviço.
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`: envio de e-mail.
- `NOTIFICACAO_EMAIL_ADMIN`, `NOTIFICACAO_EMAIL_REMETENTE`: notificações administrativas (`skip` por padrão).
- `CLIENTE_SERVICE_URL`, `VEICULO_SERVICE_URL`, `SERVICO_SERVICE_URL`, `ESTOQUE_SERVICE_URL`: URLs base dos demais serviços (por padrão, `http://localhost:<porta>` de cada um).

## Dependency-Track e SBOM

O projeto possui uma stack local do OWASP Dependency-Track no `compose.yaml` da raiz. Essa stack é independente dos microsserviços de aplicação e fica no profile `security`.

Para subir:

```powershell
docker compose --profile security up -d dependency-track-frontend
```

Serviços esperados:

- Frontend: `http://localhost:8082`
- API Server: `http://localhost:8081`
- PostgreSQL interno do Dependency-Track: `dependency-track-postgres:5432`

> **Atenção:** as portas padrão do Dependency-Track (`8081` frontend/API) coincidem com as portas de alguns microsserviços (`cliente` usa `8081`, `veiculo` usa `8082`). Se for rodar o Dependency-Track e os microsserviços ao mesmo tempo, ajuste `DEPENDENCY_TRACK_API_PORT`/`DEPENDENCY_TRACK_FRONTEND_PORT` (via `.env` na raiz) para portas livres.

Em um ambiente limpo, as credenciais iniciais do Dependency-Track são:

```text
usuário: admin
senha: admin
```

No primeiro acesso, altere a senha do usuário `admin`. Se o volume Docker já existir, vale a senha alterada anteriormente.

Para gerar o SBOM CycloneDX do projeto:

```powershell
.\mvnw.cmd org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
```

No Linux/macOS:

```bash
./mvnw org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
```

O arquivo gerado fica em:

```text
target/bom.json
```

Depois, crie um projeto no Dependency-Track e faça upload do `target/bom.json`.

Para parar apenas o Dependency-Track:

```powershell
docker compose --profile security stop dependency-track-frontend dependency-track-apiserver dependency-track-postgres
```

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

As collections e environments locais por microsserviço ficam versionados em `postman/` (ex.: `ms-cliente`, `ms-veiculo`, `ms-funcionario`, `ms-servico`, `ms-estoque`, `ms-atendimento`).

Para usar:

1. Importe a collection e o environment do serviço desejado no Postman.
2. Suba o banco do serviço (`docker compose -f services/<nome>/compose.yaml up -d`) e inicie a aplicação (`./mvnw -pl services/<nome> spring-boot:run`).
3. Para fluxos administrativos, execute primeiro `Autenticação > Login` na collection de `atendimento` (porta 8086).

O script do request de login salva o token JWT automaticamente na variável `accessToken`. As demais requests protegidas usam esse valor como Bearer Token.

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

Parar o banco de um serviço:

```bash
docker compose -f services/cliente/compose.yaml down
```

Parar e remover volumes/imagens locais do Dependency-Track:

```bash
docker compose down -v --rmi local --remove-orphans
```

Use o comando com volumes apenas quando quiser descartar os dados locais do PostgreSQL do serviço e/ou do Dependency-Track.

## Problemas Comuns

Se um serviço com autenticação (`atendimento`) falhar por causa do JWT:

- confira se `JWT_SECRET` está definido no ambiente onde o serviço roda;
- confira se a secret tem pelo menos 32 caracteres.

Se a porta estiver ocupada:

- `cliente` usa `8081`, `veiculo` usa `8082`, `funcionario` usa `8083`, `servico` usa `8084`, `estoque` usa `8085`, `atendimento` usa `8086`;
- os bancos PostgreSQL de cada serviço usam `5432` (`cliente`), `5433` (`veiculo`), `5434` (`funcionario`), `5435` (`servico`), `5436` (`estoque`) e `5437` (`atendimento`);
- Dependency-Track API usa `8081` e frontend usa `8082` por padrão (mesmas portas de `cliente`/`veiculo` — ajuste via `.env` se for usar os dois ao mesmo tempo);
- pare outros serviços nessas portas ou ajuste o `compose.yaml`/variáveis de ambiente do serviço correspondente.

Se o login falhar após limpar volumes:

- aguarde o serviço `atendimento` terminar de subir;
- confira nos logs se o admin padrão foi criado;
- use as credenciais padrão ou as variáveis `ADMIN_EMAIL` e `ADMIN_PASSWORD` configuradas.

Se o Dependency-Track demorar no primeiro start:

- aguarde o API Server ficar `healthy`;
- acompanhe com `docker logs dependency-track-apiserver --tail 100 -f`;
- o primeiro start pode baixar bases de vulnerabilidades e criar índices internos.
