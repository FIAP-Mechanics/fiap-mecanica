# Guidelines do Projeto — FIAP Mecânica

## Visão Geral

Sistema back-end de gestão integrada para uma oficina mecânica de médio porte, desenvolvido como MVP para o curso de *
*Arquitetura de Software da FIAP (Pós-Graduação)**. O sistema gerencia ordens de serviço, clientes, veículos,
funcionários, serviços e estoque de peças/insumos, com foco em qualidade de software, segurança e boas práticas
arquiteturais.

O projeto está em processo de migração de um monólito para um **monorepo de microsserviços** (padrão
**Shared-Nothing**, sem módulo `common`/`shared` de código Java). O estado atual e o histórico completo da migração
estão documentados em `docs/migracao-microsservicos.md` — consulte esse arquivo para o status detalhado de cada etapa.

---

## Stack Tecnológica

| Tecnologia                  | Versão | Uso                                                 |
|------------------------------|--------|-----------------------------------------------------|
| Java                         | 21     | Linguagem principal                                  |
| Spring Boot                  | 4.0.6  | Framework principal                                  |
| Spring Data JPA               | —      | Persistência                                         |
| Spring Security + OAuth2      | —      | Autenticação/Autorização JWT                         |
| PostgreSQL                    | —      | Banco de dados (database-per-service, via Docker)    |
| Lombok                        | —      | Redução de boilerplate                               |
| SpringDoc OpenAPI (Swagger)   | 3.0.2  | Documentação de API                                  |
| JaCoCo                        | 0.8.14 | Cobertura de testes (mínimo 80% linhas e branches)   |
| Maven                         | —      | Build multi-módulo e gerenciamento de dependências   |

---

## Arquitetura do Monorepo

O repositório é um projeto Maven multi-módulo. O `pom.xml` raiz é apenas agregador (`packaging=pom`, sem código),
contendo `dependencyManagement`/`pluginManagement` compartilhados (versões de dependências, configuração do
`jacoco-maven-plugin` com `jacoco-check` mínimo de 80% LINE/BRANCH em nível de `BUNDLE`).

```
mecanica/
├── pom.xml                      # Root POM (packaging pom), apenas dependencyManagement/properties
├── docs/                        # Documentação (plano de migração, diagramas, OpenAPI)
├── gateway/                     # API Gateway — AINDA NÃO IMPLEMENTADO (Etapa pendente)
├── infrastructure/              # docker-compose com múltiplos databases — AINDA NÃO IMPLEMENTADO (Etapa pendente)
├── services/                    # Módulos dos microsserviços (Maven multi-módulo)
│   ├── pom.xml                  # POM agregador dos módulos de serviço
│   ├── cliente/                 # Microsserviço de Clientes (porta 8081)
│   ├── veiculo/                 # Microsserviço de Veículos (porta 8082)
│   ├── funcionario/             # Microsserviço de Funcionários (porta 8083)
│   ├── servico/                 # Microsserviço de Serviços (porta 8084)
│   ├── estoque/                 # Microsserviço de Estoque/Insumos (porta 8085)
│   └── atendimento/             # Microsserviço de Ordens de Serviço + Auth/JWT embutido (porta 8086)
└── src/                         # Monólito original — ainda presente na raiz, aguardando remoção
                                  # (depende do Gateway e da infraestrutura estarem prontos primeiro)
```

Cada serviço em `services/<nome>` é um módulo Maven independente e autônomo, com seu próprio `pom.xml`,
`<Nome>Application.java`, banco de dados (`mecanica_cliente`, `mecanica_veiculo`, `mecanica_funcionario`,
`mecanica_servico`, `mecanica_estoque`, `mecanica_atendimento`) e ciclo de vida. Cada serviço possui um
**`.junie/guidelines.md` próprio** (em `services/<nome>/.junie/guidelines.md`) com detalhes específicos daquele
domínio — este arquivo cobre apenas as regras e convenções **transversais a todo o monorepo**.

### Arquitetura em Camadas (dentro de cada serviço)

Todos os serviços seguem os princípios de **Domain-Driven Design (DDD)** com a mesma arquitetura em camadas:

```
controller/         → Endpoints REST, validação de entrada, mapeamento request/response
  mapper/           → Conversão entre Request, Domain e DTO
  request/          → Objetos de entrada da API (com validações Bean Validation)
service/            → Regras de negócio e orquestração
repository/         → Acesso a dados via Spring Data JPA
domain/             → Entidades JPA e enums de domínio
dto/                → Objetos de saída da API
exception/          → Exceções de domínio tipadas
config/             → Configurações de segurança (SecurityConfig) e OpenAPI (OpenApiConfig)
infra/              → Handler global de exceções, DTOs de erro, enums de código de erro (sem o segmento "configs")
client/             → (apenas em `atendimento`) Clients HTTP (RestClient) para os demais serviços + DTOs de integração
```

### Regras Arquiteturais

- **Sem módulo `common`/`shared`**: cada serviço duplica sua própria infraestrutura básica (`GlobalExceptionHandler`,
  `RespostaErro`, `ErroDetalhe`, `CodigoErro`, `BaseException`, etc.) — não criar dependências de código Java entre
  serviços.
- **Comunicação entre serviços**: exclusivamente via HTTP síncrono (`RestClient`), nunca por acesso direto a banco de
  dados ou dependência Maven entre módulos de serviço. Hoje só `atendimento` consome os demais serviços
  (`cliente`, `veiculo`, `servico`, `estoque`) através de clients HTTP próprios em `client/`.
- **Controllers** nunca acessam repositories diretamente — sempre passam pelo service.
- **Services** contêm toda a lógica de negócio; não devem retornar entidades JPA diretamente para o controller — usam
  DTOs.
- **Mappers** são classes utilitárias estáticas responsáveis pela conversão entre camadas.
- **Domínios** são entidades JPA puras, sem lógica de apresentação. Em `atendimento`, os domínios são
  **desacoplados** dos demais serviços (sem `@ManyToOne` para `Cliente`/`Veiculo`/`Servico`/`Insumo` — apenas IDs e
  snapshots de nome/valor).
- **Exceções** são tipadas por domínio (ex: `ClienteNotFound`, `VeiculoInativoException`) e tratadas centralmente pelo
  `GlobalExceptionHandler` de cada serviço.
- O schema do banco é gerado automaticamente via **JPA DDL-AUTO** (sem Flyway/Liquibase).

---

## Domínios Principais

### Cliente (`services/cliente`, porta 8081)

- Identificado por CPF ou CNPJ (campo `documento`, único).
- Possui: nome, email, telefone, endereço (embedded).
- **Não possui soft delete** — clientes não são desativados.

### Veiculo (`services/veiculo`, porta 8082)

- Identificado por placa (única).
- Possui: marca, modelo, ano, ativo (soft delete).
- No monólito original, relacionamento **N:N com Cliente** representava o histórico de veículos que um cliente já
  levou à oficina; no microsserviço, esse histórico é resolvido via chamadas HTTP a partir de `atendimento`, sem
  relacionamento JPA entre serviços.

### Funcionario (`services/funcionario`, porta 8083)

- CRUD simples de funcionários (nome, email, senha hash, funcao, ativo/soft delete), **sem** autenticação/JWT.
- **Roles (Funcao):** `ADMIN`, `MECANICO`, `ATENDENTE`.
- A autenticação (login/emissão e validação de JWT) vive em `services/atendimento`, não neste serviço.

### Servico (`services/servico`, porta 8084)

- Tipos de serviços oferecidos (ex: troca de óleo, alinhamento).
- Possui soft delete (`ativo`).

### Insumo / Estoque (`services/estoque`, porta 8085)

- `Insumo` define os **tipos** de peças e consumíveis (ex: pneu, óleo); `Estoque` controla as **quantidades
  disponíveis** de cada insumo na oficina. Ambos com soft delete (`ativo`).
- Expõe o endpoint adicional `POST /estoque/deduzir`, consumido via `EstoqueClient` pelo serviço `atendimento`.

### OrdemDeServico / Atendimento (`services/atendimento`, porta 8086)

- Entidade central do sistema, já implementada — gerenciada pelo `AtendimentoController`.
- **Status possíveis:** `RECEBIDA` → `EM_DIAGNOSTICO` → `AGUARDANDO_APROVACAO` → `EM_EXECUCAO` → `FINALIZADA` →
  `ENTREGUE`.
- Referencia cliente e veículo apenas por ID (`clienteId`, `veiculoId`), com snapshots de nome/valor para serviços e
  insumos; orçamento calculado automaticamente; timestamps de cada transição de status.
- Também concentra a **autenticação do sistema** (`AuthController`, `AuthService`, `JwtTokenService`,
  `FuncionarioUserDetailsService`) e a feature de **Templates de notificação por e-mail**
  (`TemplateController`/`TemplateService`/`EmailNotificationService`), que notifica o cliente por e-mail nas
  transições de diagnóstico, finalização e entrega do veículo.

---

## Segurança

- **Rotas públicas (sem autenticação):** todos os endpoints `GET` (leitura), em todos os serviços.
- **Rotas protegidas (requerem JWT):** `POST`, `PUT`, `DELETE` em todos os recursos administrativos.
- Autenticação baseada em **JWT** com roles derivadas do campo `Funcao` do `Funcionario`.
- **Estado atual (transversal, pendência conhecida):** apenas `services/atendimento` possui segurança real, com
  `oauth2ResourceServer` + `JwtDecoder`/`JwtEncoder` completos (emite e valida JWT). Os demais 5 serviços
  (`cliente`, `veiculo`, `funcionario`, `servico`, `estoque`) ainda estão com `SecurityConfig` em `permitAll`
  temporário — a validação real do JWT (sem emissão) nesses serviços depende da definição do Gateway (Etapa 4 do
  plano de migração) e ainda precisa ser implementada.
- Dados sensíveis (CPF/CNPJ, placa) devem ser validados com Bean Validation e/ou validadores customizados.

---

## Padrões de Desenvolvimento

### Nomenclatura

- **Requests:** `CadastrarXxxRequest`, `AtualizarXxxRequest`
- **DTOs de saída:** `XxxDto`
- **Mappers:** `XxxMapper` (métodos estáticos: `toDto`, `toEntity`)
- **Exceptions:** `XxxNotFound`, `XxxInativoException`, `XxxJaAtivoException`
- **Controllers:** `XxxController` com `@RequestMapping("/xxx")` em português (ex: `/cliente`, `/veiculo`)
- **Endpoints:** substantivos no singular, em português

### Boas Práticas Obrigatórias

- Usar `@Valid` nos `@RequestBody` dos controllers.
- Toda exceção de negócio deve estender `BaseException` e ser mapeada no `GlobalExceptionHandler` do respectivo
  serviço.
- Usar `@Builder` + `@Data` + `@NoArgsConstructor` + `@AllArgsConstructor` nas entidades e DTOs com Lombok.
- Soft delete: entidades com `ativo` devem ter endpoints de ativação/desativação separados (não usar DELETE físico).
- Documentar todos os endpoints com anotações Swagger (`@Operation`, `@ApiResponses`, `@Parameter`, `@Tag`).
- Ao criar um novo microsserviço ou alterar um existente, seguir o `.junie/guidelines.md` específico daquele serviço
  (em `services/<nome>/.junie/guidelines.md`), além destas regras transversais.
- Após a conclusão de todo desenvolvimento, revisar o código adicionado e procurar possíveis refatorações seguindo os
  melhores padrões de desenvolvimento e arquitetura.
- Seguir o skill de documentação Swagger disponível em `.junie/skills/swagger-documentation/SKILL.md`.
- Seguir o skill de testes unitários disponível em `.junie/skills/unit-test/SKILL.md`.

### Testes

- Cobertura mínima obrigatória: **80% de linhas e branches** por serviço, validada via `jacoco-check` (elemento
  `BUNDLE`) — configurado no `pom.xml` raiz e ativado individualmente em cada `services/<nome>/pom.xml`.
- Escrever testes unitários para services e mappers.
- Escrever testes de integração para os principais fluxos dos controllers.
- Consultar o skill de testes antes de implementar qualquer classe de teste.
- Rodar os testes de um serviço isoladamente com `./mvnw -pl services/<nome> -am verify`, ou de todos os serviços com
  `./mvnw -pl services/cliente,services/veiculo,services/funcionario,services/servico,services/estoque,services/atendimento -am verify`.

---

## Infraestrutura

- Banco de dados PostgreSQL provisionado via **Docker Compose** (`compose.yaml`, na raiz — ainda referente ao
  monólito; a migração para `infrastructure/docker-compose.yml` com um database por serviço é uma etapa pendente).
- Schema gerenciado por **JPA DDL-AUTO** — não usar Flyway ou Liquibase.
- Para rodar localmente: subir o Docker Compose antes de iniciar cada aplicação.
- Cada serviço roda de forma independente na sua própria porta (8081 a 8086); ainda não há API Gateway — cada serviço
  é acessado diretamente na sua porta até a Etapa 4 do plano de migração ser concluída.

# Mais considerações sobre o comportamento do agente

- Comandos de terminal (build, testes, verificação de estado) podem ser utilizados normalmente pelo agente.
- Caso alguma informação não fique clara, peça para que eu (desenvolvedor) explique ou forneça mais detalhes.
- De maneira alguma assuma alguma informação que não seja explicitamente dita no contexto.
- Sempre que possível, peça para que eu (desenvolvedor) explique o motivo de uma decisão ou escolha de implementação,
  caso acredite que o padrão de desenvolvimento não seja adequado.
- Não implemente testes unitários ou corrija-os à menos que seja explicitamente solicitado.
