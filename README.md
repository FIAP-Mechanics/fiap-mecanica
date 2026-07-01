# Oficina Mecânica

API REST para gerenciamento de uma oficina mecânica. O sistema cobre cadastro de clientes, veículos, serviços, estoque/insumos, abertura e acompanhamento de ordens de serviço, cálculo de orçamento, notificações, autenticação JWT e controle de permissões por perfil.

## Objetivo

O objetivo do projeto é apoiar o fluxo principal de atendimento de uma oficina, desde a chegada do cliente até a entrega do veículo, mantendo rastreabilidade da ordem de serviço e separando as responsabilidades entre atendente, mecânico, administrador e cliente.


## Fluxo da Ordem de Serviço

1. O cliente solicita o atendimento para a atendente.
2. A atendente identifica ou cadastra o cliente e o veículo.
3. A atendente inicia a ordem de serviço em `POST /atendimento/iniciar`.
4. A ordem nasce com status `RECEBIDA`.
5. O mecânico inicia o diagnóstico em `PATCH /atendimento/{id}/diagnostico/iniciar`.
6. O mecânico adiciona diagnóstico, serviços e insumos em `POST /atendimento/{id}/diagnostico`.
7. O sistema calcula o orçamento e altera a OS para `AGUARDANDO_APROVACAO`.
8. O cliente acompanha a OS por `GET /atendimento/{id}` e decide a aprovação em comunicação manual com a atendente.
9. Se aprovado, a atendente registra em `POST /atendimento/{id}/aprovar`; o sistema baixa os insumos do estoque e muda a OS para `EM_EXECUCAO`.
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

As notificações usam templates cadastrados no sistema.

- Cliente: recebe aviso para autorização de orçamento, retirada do veículo e confirmação de veículo retirado.
- Funcionários: recebem aviso quando o estoque não possui quantidade suficiente e há necessidade de reposição.

As configurações de e-mail ficam no `.env`. Em ambiente local, use `skip` nas variáveis `NOTIFICACAO_EMAIL_ADMIN` e `NOTIFICACAO_EMAIL_REMETENTE` para não enviar e-mails reais. Quando o envio está em `skip`, o sistema não busca templates de notificação.

Para habilitar envio real, configure um remetente válido e mantenha os templates cadastrados no banco.

## Perfis e Permissões

As APIs administrativas usam JWT. O token carrega a role do funcionário autenticado.

- `ADMIN`: acesso total ao sistema, incluindo funcionários, templates, relatórios e operações administrativas.
- `ATENDENTE`: opera clientes, veículos, estoque, abertura de atendimento, aprovação de orçamento, cancelamento e entrega da OS.
- `MECANICO`: opera serviços, estoque, diagnóstico, inclusão de serviços/insumos na OS e finalização técnica.
- Cliente: não possui login neste projeto; acompanha a OS pelo endpoint público `GET /atendimento/{id}`.

Endpoints públicos:

- `POST /auth/login`
- `GET /atendimento/{id}`
- `/swagger-ui/**`
- `/v3/api-docs/**`

## Requisitos

Para rodar com Docker:

- Docker e Docker Compose
- Git

Não é necessário instalar Java localmente para o fluxo com Docker. A imagem da aplicação já contém o runtime necessário.

Para rodar a API fora do Docker:

- Java 21
- Git

O projeto possui Maven Wrapper, então não é necessário instalar Maven localmente.

## Configuração Local

Crie um arquivo `.env` a partir do exemplo versionado:

```powershell
Copy-Item .env.example .env
```

No Linux/macOS:

```bash
cp .env.example .env
```

Confira principalmente a variável `JWT_SECRET`. Ela precisa ter pelo menos 32 caracteres:

```env
JWT_SECRET=troque-por-uma-secret-com-pelo-menos-32-caracteres
```

O `.env` é ignorado pelo Git e deve ser usado apenas para configuração local.

Variáveis principais:

- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`: banco local da aplicação.
- `JWT_SECRET`, `JWT_EXPIRATION_SECONDS`, `JWT_ISSUER`: autenticação JWT.
- `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NOME`: admin criado automaticamente na inicialização.
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`: envio de e-mail.
- `NOTIFICACAO_EMAIL_ADMIN`, `NOTIFICACAO_EMAIL_REMETENTE`: notificações administrativas.
- `DEPENDENCY_TRACK_*`: portas e banco local do Dependency-Track.

## Subir com Docker

Este é o caminho mais simples para rodar a aplicação localmente, pois sobe a API e o PostgreSQL juntos:

```powershell
docker compose up -d --build
```

Verifique se os containers estão rodando:

```powershell
docker compose ps
```

Serviços esperados:

- API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Logs da API:

```powershell
docker logs fiap-mecanica-app --tail 100 -f
```

## Dependency-Track e SBOM

O projeto também possui uma stack local do OWASP Dependency-Track no Docker Compose. Ela fica no profile `security` para não deixar o `docker compose up` padrão mais pesado.

Para subir:

```powershell
docker compose --profile security up -d dependency-track-frontend
```

Serviços esperados:

- Frontend: `http://localhost:8082`
- API Server: `http://localhost:8081`
- PostgreSQL interno do Dependency-Track: `dependency-track-postgres:5432`

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

Ao iniciar, a aplicação cria automaticamente um funcionário administrador caso ele ainda não exista.

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
POST http://localhost:8080/auth/login
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

A collection e o environment local ficam versionados em `postman/`:

- `postman/oficina-mecanica.postman_collection.json`
- `postman/oficina-mecanica.local.postman_environment.json`

Para usar:

1. Importe os dois arquivos no Postman.
2. Selecione o environment `Oficina mecânica - Local Docker`.
3. Suba a aplicação com `docker compose up -d --build`.
4. Execute `Autenticação > Login`.

O script do request de login salva o token JWT automaticamente na variável `accessToken`. As demais requests protegidas usam esse valor como Bearer Token.

O environment também traz variáveis de apoio, como `clienteId`, `veiculoId`, `servicoId`, `insumoId`, `ordemServicoId` e `templateCodigo`. Ajuste esses valores conforme os dados criados no seu banco local.

## Rodar a API Fora do Docker

Esta etapa não é necessária para usar o Postman se a aplicação já foi iniciada com Docker Compose. Use esta alternativa apenas quando quiser rodar a API pela máquina, por exemplo para debugar com breakpoints.

Suba apenas o banco:

```powershell
docker compose up -d postgres
```

Inicie a API:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

Nesse modo, a aplicação usa as configurações de `src/main/resources/application.yaml` e importa automaticamente o arquivo `.env`.

## Testes

Windows:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

Os testes usam profile de teste com H2 em memória, então não dependem do PostgreSQL local.

Para gerar relatório de cobertura JaCoCo:

```powershell
.\mvnw.cmd test jacoco:report
```

O relatório fica em:

```text
target/site/jacoco/index.html
```

## Parar e Limpar Docker

Parar containers:

```powershell
docker compose down
```

Parar e remover volumes/imagens locais do projeto:

```powershell
docker compose down -v --rmi local --remove-orphans
```

Use o comando com volumes apenas quando quiser descartar os dados locais do PostgreSQL e do Dependency-Track.

## Estrutura do Projeto

```text
src/main/java/com/fiap/mecanica
├── config              # Segurança, OpenAPI e inicialização do admin
├── controller          # Endpoints REST
├── controller/request  # Payloads de entrada
├── controller/mapper   # Conversão entre domínio e DTO
├── domain              # Entidades e enums de domínio
├── dto                 # Objetos de resposta
├── exception           # Exceções de negócio
├── infra/configs       # Tratamento global de erros e enums auxiliares
├── repository          # Repositórios JPA
├── security            # Carregamento do usuário autenticado
├── service             # Regras de negócio
└── validation          # Validações customizadas
```

Arquivos úteis na raiz:

- `compose.yaml`: stack Docker local.
- `Dockerfile`: build da aplicação.
- `.env.example`: exemplo de configuração local.
- `postman/`: collection e environment para testes manuais.
- `pom.xml`: dependências, testes, JaCoCo e CycloneDX.

## Problemas Comuns

Se a aplicação falhar por causa do JWT:

- confira se `JWT_SECRET` existe no `.env`;
- confira se a secret tem pelo menos 32 caracteres.

Se a porta estiver ocupada:

- API usa `8080`;
- PostgreSQL da aplicação usa `5432`;
- Dependency-Track API usa `8081`;
- Dependency-Track frontend usa `8082`;
- pare outros serviços nessas portas ou ajuste o `compose.yaml`/`.env`.

Se o login falhar após limpar volumes:

- aguarde a aplicação terminar de subir;
- confira nos logs se o admin padrão foi criado;
- use as credenciais padrão ou as variáveis `ADMIN_EMAIL` e `ADMIN_PASSWORD` configuradas.

Se o Dependency-Track demorar no primeiro start:

- aguarde o API Server ficar `healthy`;
- acompanhe com `docker logs dependency-track-apiserver --tail 100 -f`;
- o primeiro start pode baixar bases de vulnerabilidades e criar índices internos.
