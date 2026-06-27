---
name: unit-test-skill
description: Regras e guidelines implementação de testes unitários e garantia de cobertura do projeto
---

# Guidelines de Testes Unitários — Junie Skill

Use essa skills sempre que houver desenvolvimento de testes unitários.

## Objetivo

Ao receber uma solicitação para gerar testes unitários de uma classe Java (service, controller ou qualquer outro
componente), siga rigorosamente estas diretrizes para produzir uma suíte completa, pronta para compilação, sem
pseudocódigo e sem trechos incompletos.

---

## Tecnologias obrigatórias

- JUnit 5 (Jupiter)
- Mockito
- AssertJ

---

## Regras de implementação

- Utilizar `@ExtendWith(MockitoExtension.class)`.
- Utilizar `@Mock`, `@InjectMocks` e `@Captor` quando apropriado.
- Não utilizar `@SpringBootTest`.
- Não utilizar banco em memória.
- Não utilizar contexto Spring.
- Não utilizar Reflection.
- Não alterar o código de produção.
- Seguir o padrão **Arrange / Act / Assert**.
- Garantir que cada teste seja independente.
- Eliminar duplicação de código através de métodos auxiliares privados.
- Utilizar `ArgumentCaptor` quando necessário.
- Validar interações com mocks utilizando `verify`.
- Validar ausência de interações utilizando `never()`, `verifyNoInteractions()` ou `verifyNoMoreInteractions()` quando
  aplicável.

---

## Padrão de nomenclatura

- Todo o código deve estar em **português**.
- Todos os métodos de teste devem estar em português.
- Variáveis auxiliares devem estar em português.
- Métodos auxiliares devem estar em português.
- Builders e factories de teste devem estar em português.
- Seguir o padrão:
    - `deveRetornarFuncionarioQuandoIdExistir`
    - `deveLancarExcecaoQuandoFuncionarioNaoExistir`
    - `deveAtualizarSomenteCamposInformados`
    - `deveRealizarExclusaoLogicaComSucesso`

---

## Cobertura obrigatória

Para cada método público da classe testada:

- Cobrir todos os cenários de sucesso.
- Cobrir todos os cenários de erro.
- Cobrir todos os cenários de exceção.
- Cobrir todos os caminhos condicionais.
- Cobrir todos os retornos possíveis.
- Cobrir todos os tratamentos de `Optional`.
- Cobrir todos os comportamentos relacionados a enums.
- Cobrir cenários com valores nulos quando aplicável.
- Cobrir cenários de atualização parcial.
- Cobrir cenários de exclusão lógica.
- Cobrir cenários de persistência.

---

## Validações obrigatórias

- Validar os objetos retornados.
- Validar os atributos alterados.
- Validar os atributos não alterados.
- Validar as exceções lançadas.
- Validar as mensagens das exceções quando existirem.
- Validar as interações com o repositório / service (dependendo da camada testada).
- Validar os argumentos enviados ao colaborador via `ArgumentCaptor`.

---

## Qualidade esperada

- Código limpo e legível.
- Sem comentários desnecessários.
- Sem números mágicos — utilizar constantes (`private static final`).
- Utilizar métodos de criação de objetos de teste (builders/factories privados).
- Produzir testes fáceis de manter.
- Evitar testes redundantes.

---

## Estrutura esperada da classe de testes

```java

@ExtendWith(MockitoExtension.class)
class NomeDaClasseTest {

    // Constantes
    private static final Long ID_EXISTENTE = 1L;
    // ...

    // Mocks e subject under test
    @Mock
    private Dependencia dependencia;

    @InjectMocks
    private ClasseSobTeste classeSobTeste;

    @Captor
    private ArgumentCaptor<Entidade> entidadeCaptor;

    // Testes agrupados por método público, com comentários de seção
    // ===================== nomeDoMetodo =====================

    @Test
    void deveFazerAlgoQuandoCondicao() {
        // Arrange
        // Act
        // Assert
    }

    // Métodos auxiliares privados
    private Entidade criarEntidadeAtiva() { ...}
}
```

---

## Ao finalizar

Reveja a implementação e confirme que:

1. Todos os métodos públicos da classe possuem cobertura completa de linhas e branches.
2. Todos os testes compilam e passam sem erros.
3. Nenhuma regra acima foi violada.

---