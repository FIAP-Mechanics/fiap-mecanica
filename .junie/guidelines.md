# Guidelines do Projeto — FIAP Mecânica

## Visão Geral

Sistema back-end de gestão integrada para uma oficina mecânica de médio porte, desenvolvido como MVP para o curso de *
*Arquitetura de Software da FIAP (Pós-Graduação)**. O sistema gerencia ordens de serviço, clientes, veículos,
funcionários, serviços e estoque de peças/insumos, com foco em qualidade de software, segurança e boas práticas
arquiteturais.

---

## Stack Tecnológica

| Tecnologia                  | Versão | Uso                                                |
|-----------------------------|--------|----------------------------------------------------|
| Java                        | 21     | Linguagem principal                                |
| Spring Boot                 | 4.0.6  | Framework principal                                |
| Spring Data JPA             | —      | Persistência                                       |
| Spring Security + OAuth2    | —      | Autenticação/Autorização JWT                       |
| PostgreSQL                  | —      | Banco de dados (via Docker Compose)                |
| Lombok                      | —      | Redução de boilerplate                             |
| SpringDoc OpenAPI (Swagger) | 3.0.2  | Documentação de API                                |
| JaCoCo                      | 0.8.14 | Cobertura de testes (mínimo 90% linhas e branches) |
| Maven                       | —      | Build e gerenciamento de dependências              |

---

## Arquitetura

O projeto segue os princípios de **Domain-Driven Design (DDD)** com arquitetura em camadas bem definidas:

```
controller/         → Endpoints REST, validação de entrada, mapeamento request/response
  mapper/           → Conversão entre Request, Domain e DTO
  request/          → Objetos de entrada da API (com validações Bean Validation)
service/            → Regras de negócio e orquestração
repository/         → Acesso a dados via Spring Data JPA
domain/             → Entidades JPA e enums de domínio
dto/                → Objetos de saída da API
exception/          → Exceções de domínio tipadas
config/             → Configurações de segurança e OpenAPI
infra/configs/      → Handler global de exceções, DTOs de erro, enums de código de erro
```

### Regras Arquiteturais

- **Controllers** nunca acessam repositories diretamente — sempre passam pelo service.
- **Services** contêm toda a lógica de negócio; não devem retornar entidades JPA diretamente para o controller — usam
  DTOs.
- **Mappers** são classes utilitárias estáticas responsáveis pela conversão entre camadas.
- **Domínios** são entidades JPA puras, sem lógica de apresentação.
- **Exceções** são tipadas por domínio (ex: `ClienteNotFound`, `VeiculoInativoException`) e tratadas centralmente pelo
  `GlobalExceptionHandler`.
- O schema do banco é gerado automaticamente via **JPA DDL-AUTO** (sem Flyway/Liquibase).

---

## Domínios Principais

### Cliente

- Identificado por CPF ou CNPJ (campo `documento`, único).
- Possui: nome, email, telefone, endereço (embedded).
- **Não possui soft delete** — clientes não são desativados.

### Veiculo

- Identificado por placa (única).
- Possui: marca, modelo, ano, ativo (soft delete).
- Relacionamento **N:N com Cliente** — representa o histórico de veículos que um cliente já levou à oficina, não uma
  relação de posse.

### Funcionario

- Entidade de autenticação do sistema.
- Possui: nome, email, senha (hash), funcao (enum), ativo (soft delete).
- **Roles (Funcao):** `ADMIN`, `MECANICO`, `ATENDENTE`.
- Autenticação via JWT — ainda a ser implementada no `SecurityConfig`.

### Servico

- Tipos de serviços oferecidos (ex: troca de óleo, alinhamento).
- Possui soft delete (`ativo`).

### Insumo

- Define os **tipos** de peças e consumíveis (ex: pneu, óleo).

### Estoque

- Controla as **quantidades disponíveis** de cada insumo na oficina.
- Possui soft delete (`ativo`).

### OrdemDeServico (Atendimento)

- Entidade central do sistema — **ainda a ser implementada**.
- Gerenciada pelo `AtendimentoController`.
- **Status possíveis:** `RECEBIDA` → `EM_DIAGNOSTICO` → `AGUARDANDO_APROVACAO` → `EM_EXECUCAO` → `FINALIZADA` →
  `ENTREGUE`.
- Deve conter: cliente, veículo, lista de serviços, lista de peças/insumos, orçamento calculado automaticamente, status
  atual, timestamps de cada transição.

---

## Segurança

- **Rotas públicas (sem autenticação):** todos os endpoints `GET` (leitura).
- **Rotas protegidas (requerem JWT):** `POST`, `PUT`, `DELETE` em todos os recursos administrativos.
- Autenticação baseada em **JWT** com roles derivadas do campo `Funcao` do `Funcionario`.
- O `SecurityConfig` atual está com `permitAll` temporariamente — a implementação de JWT é uma das próximas tarefas.
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
- Toda exceção de negócio deve estender `BaseException` e ser mapeada no `GlobalExceptionHandler`.
- Usar `@Builder` + `@Data` + `@NoArgsConstructor` + `@AllArgsConstructor` nas entidades e DTOs com Lombok.
- Soft delete: entidades com `ativo` devem ter endpoints de ativação/desativação separados (não usar DELETE físico).
- Documentar todos os endpoints com anotações Swagger (`@Operation`, `@ApiResponses`, `@Parameter`, `@Tag`).
- Após a conclusão de todo desenvolvimento, revisar o código adicionado e procurar prossíveis refatorações seguindo os 
  melhores padrões de desenvolvimento e arquitetura.
- Seguir o skill de documentação Swagger disponível em `.junie/skills/swagger-documentation/SKILL.md`.
- Seguir o skill de testes unitários disponível em `.junie/skills/unit-test/SKILL.md`.

### Testes

- Cobertura mínima obrigatória: **90% de linhas e branches** (enforced pelo JaCoCo).
- Escrever testes unitários para services e mappers.
- Escrever testes de integração para os principais fluxos dos controllers.
- Consultar o skill de testes antes de implementar qualquer classe de teste.

---

## Infraestrutura

- Banco de dados PostgreSQL provisionado via **Docker Compose** (`compose.yaml`).
- Schema gerenciado por **JPA DDL-AUTO** — não usar Flyway ou Liquibase.
- Para rodar localmente: subir o Docker Compose antes de iniciar a aplicação.

# Mais considerações sobre o comportamento do agente

- Nunca utilize comandos terminais, caso seja necessário, peça para que eu (desenvolvedor) execute os comandos e cole a
  resposta.
- Caso alguma informação não fique clara, peça para que eu (desenvolvedor) explique ou forneça mais detalhes.
- De maneira alguma assuma alguma informação que não seja explicitamente dita no contexto.
- Sempre que possível, peça para que eu (desenvolvedor) explique o motivo de uma decisão ou escolha de implementação,
  caso acredite que o padrão de desenvolvimento não seja adequado.
- Não implemente testes unitários ou corrija-os à menos que seja explicitamente solicitado.
