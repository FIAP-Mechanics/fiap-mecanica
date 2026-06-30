# Oficina Mecânica

API para gerenciamento de uma oficina mecânica, com cadastro de clientes, veículos, serviços, estoque/insumos, ordens de serviço, autenticação JWT e controle de permissões por perfil.

## Requisitos

- Docker e Docker Compose
- Git

Para rodar com Docker, não é necessário instalar Java localmente. A imagem da aplicação já contém o runtime necessário.

Java 21 só é necessário se você optar por rodar a API fora do Docker, pela própria máquina. O projeto já possui Maven Wrapper, então não é necessário instalar Maven localmente.

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

## Subir Com Docker

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

Para acompanhar logs:

```powershell
docker logs fiap-mecanica-app --tail 100 -f
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

## Login

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

## Alternativa: Rodar a API Fora do Docker

Esta etapa não é necessária para usar o Postman se a aplicação já foi iniciada com Docker Compose. Use esta alternativa apenas quando quiser rodar a API pela máquina, por exemplo para debugar com breakpoints.

Nesse caso, rode apenas o banco no Docker e inicie a API pela máquina:

```powershell
docker compose up -d postgres
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

Nesse modo, a aplicação usa as configurações de `src/main/resources/application.yaml` e importa automaticamente o arquivo `.env`.

## Rodar Testes

Windows:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

Os testes usam profile de teste com H2 em memória, então não dependem do PostgreSQL local.

## Parar e Limpar Docker

Parar containers:

```powershell
docker compose down
```

Parar e remover volumes/imagens locais do projeto:

```powershell
docker compose down -v --rmi local --remove-orphans
```

Use o comando com volumes apenas quando quiser descartar os dados locais do PostgreSQL.

## Problemas Comuns

Se a aplicação falhar por causa do JWT:

- confira se `JWT_SECRET` existe no `.env`;
- confira se a secret tem pelo menos 32 caracteres.

Se a porta estiver ocupada:

- API usa `8080`;
- PostgreSQL usa `5432`;
- pare outros serviços nessas portas ou ajuste o `compose.yaml`.

Se o login falhar após limpar volumes:

- aguarde a aplicação terminar de subir;
- confira nos logs se o admin padrão foi criado;
- use as credenciais padrão ou as variáveis `ADMIN_EMAIL` e `ADMIN_PASSWORD` configuradas.
