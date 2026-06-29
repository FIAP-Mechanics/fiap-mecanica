---
name: swagger-documentation-skill
description: Regras e guidelines para documentação da api do projeto com Swagger/OpenAPI
---

# Guidelines de Documentação Swagger/OpenAPI — Junie Skill

Use essa skill sempre que houver algum desenvolvimento de uma, ou trechos de uma, API com Swagger/OpenAPI.

## Objetivo

Ao implementar qualquer novo controller, DTO, request, response ou enum em Java, adicione automaticamente as anotações
Swagger/OpenAPI completas, seguindo rigorosamente estas diretrizes. A documentação deve ser gerada junto com o código de
produção, nunca como etapa separada.

---

## Dependência obrigatória

Verificar se o `pom.xml` já contém a dependência abaixo. Caso não contenha, adicioná-la:

```xml

<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.2</version>
</dependency>
```

---

## Configuração global obrigatória

Criar (ou verificar existência de) uma classe `OpenApiConfig` no pacote `config`:

```java

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nome da API")
                        .description("Descrição da API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Time")
                                .email("contato@empresa.com")));
    }
}
```

---

## Regras por camada

### Controllers

- Anotar a classe com `@Tag(name = "...", description = "...")`.
- Anotar cada método com `@Operation(summary = "...", description = "...")`.
- Anotar cada método com `@ApiResponses` contendo todos os códigos HTTP possíveis:
    - `200` — sucesso
    - `400` — dados inválidos (quando aplicável)
    - `404` — recurso não encontrado (quando aplicável)
    - `409` — conflito de estado (quando aplicável)
- Usar `content = @Content` em respostas sem corpo.
- Anotar parâmetros de path com `@Parameter(description = "...")`.

Exemplo:

```java

@Tag(name = "Funcionários", description = "Operações de gerenciamento de funcionários")
@RestController
@RequestMapping("/funcionarios")
public class FuncionarioController {

    @Operation(summary = "Buscar por ID", description = "Retorna os dados de um funcionário pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado",
                    content = @Content(schema = @Schema(implementation = FuncionarioDto.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public FuncionarioDto get(@Parameter(description = "ID do funcionário") @PathVariable Long id) { ...}
}
```

### DTOs e Records de Request/Response

- Anotar a classe/record com `@Schema(description = "...")`.
- Anotar cada campo com `@Schema(description = "...", example = "...")`.
- Para campos de escrita apenas, usar `accessMode = Schema.AccessMode.WRITE_ONLY`.

Exemplo:

```java

@Schema(description = "Dados do funcionário")
public record FuncionarioDto(
        @Schema(description = "Identificador único", example = "1") Long id,
        @Schema(description = "E-mail", example = "joao@empresa.com") String email,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Schema(description = "Senha", accessMode = Schema.AccessMode.WRITE_ONLY) String senha) {
}
```

### Enums

- Anotar o enum com `@Schema(description = "...")`.
- Anotar cada valor com `@Schema(description = "...")`.

Exemplo:

```java

@Schema(description = "Função do funcionário na oficina")
public enum Funcao {
    @Schema(description = "Administrador do sistema") ADMIN,
    @Schema(description = "Mecânico da oficina") MECANICO,
    @Schema(description = "Atendente da oficina") ATENDENTE
}
```

---

## Regras gerais

- Nunca deixar `summary` ou `description` vazios.
- Sempre incluir exemplos realistas nos campos `example`.
- Cobrir todos os códigos de resposta HTTP que o endpoint pode retornar.
- Não duplicar informações já expressas pelo tipo Java (evitar `@Schema` redundante em tipos primitivos óbvios).
- Manter as anotações Swagger junto ao código que documentam, nunca em arquivos separados.

---

## Ao finalizar nova implementação

Confirme que:

1. Todos os novos controllers possuem `@Tag`, `@Operation` e `@ApiResponses` em cada endpoint.
2. Todos os novos DTOs, requests e responses possuem `@Schema` na classe e em cada campo.
3. Todos os novos enums possuem `@Schema` na declaração e em cada valor.
4. A classe `OpenApiConfig` existe e está configurada.
5. A dependência `springdoc-openapi-starter-webmvc-ui` está no `pom.xml`.