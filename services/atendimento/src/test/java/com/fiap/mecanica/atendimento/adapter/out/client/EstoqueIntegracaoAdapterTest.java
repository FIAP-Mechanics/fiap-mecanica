package com.fiap.mecanica.atendimento.adapter.out.client;

import com.fiap.mecanica.atendimento.adapter.out.client.dto.DeduzirEstoqueItemDto;
import com.fiap.mecanica.atendimento.adapter.out.client.dto.EstoqueIntegracaoDto;
import com.fiap.mecanica.atendimento.adapter.out.client.dto.InsumoIntegracaoDto;
import com.fiap.mecanica.atendimento.application.port.out.EstoqueIntegracaoGateway;
import com.fiap.mecanica.atendimento.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.atendimento.exception.InsumoNaoEncontradoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class EstoqueIntegracaoAdapterTest {

    private static final Long INSUMO_ID = 1L;
    private static final String BASE_URL = "http://estoque-service";
    private static final String AUTHORIZATION_HEADER = "Bearer token-interno";
    private static final String NOME_INSUMO = "Filtro de oleo";
    private static final BigDecimal PRECO_UNITARIO = BigDecimal.valueOf(45);

    @Mock
    private InternalServiceTokenProvider internalServiceTokenProvider;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    private MockedStatic<RestClient> restClientMockedStatic;

    private EstoqueIntegracaoAdapter estoqueIntegracaoAdapter;

    @BeforeEach
    void configurarAdapter() {
        restClientMockedStatic = mockStatic(RestClient.class);
        restClientMockedStatic.when(() -> RestClient.create(BASE_URL)).thenReturn(restClient);
        estoqueIntegracaoAdapter = new EstoqueIntegracaoAdapter(
                BASE_URL, internalServiceTokenProvider, new ObjectMapper());
    }

    @AfterEach
    void finalizarMockStatic() {
        restClientMockedStatic.close();
    }

    // ===================== buscarInsumo =====================

    @Test
    void deveRetornarInsumoIntegracaoQuandoBuscarComSucesso() {
        EstoqueIntegracaoDto dto = new EstoqueIntegracaoDto(
                new InsumoIntegracaoDto(INSUMO_ID, NOME_INSUMO, PRECO_UNITARIO), 10L);
        when(internalServiceTokenProvider.obterAuthorizationHeader()).thenReturn(AUTHORIZATION_HEADER);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/estoque/{insumoId}", INSUMO_ID)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(EstoqueIntegracaoDto.class)).thenReturn(dto);

        EstoqueIntegracaoGateway.InsumoIntegracao resultado = estoqueIntegracaoAdapter.buscarInsumo(INSUMO_ID);

        assertThat(resultado.id()).isEqualTo(INSUMO_ID);
        assertThat(resultado.nome()).isEqualTo(NOME_INSUMO);
        assertThat(resultado.precoUnitario()).isEqualTo(PRECO_UNITARIO);
    }

    @Test
    void deveLancarInsumoNaoEncontradoExceptionQuandoInsumoNaoExistir() {
        when(internalServiceTokenProvider.obterAuthorizationHeader()).thenReturn(AUTHORIZATION_HEADER);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/estoque/{insumoId}", INSUMO_ID)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(EstoqueIntegracaoDto.class))
                .thenThrow(criarHttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> estoqueIntegracaoAdapter.buscarInsumo(INSUMO_ID))
                .isInstanceOf(InsumoNaoEncontradoException.class)
                .hasMessageContaining(String.valueOf(INSUMO_ID));
    }

    // ===================== deduzirEstoque =====================

    @Test
    void deveDeduzirEstoqueComSucesso() {
        List<EstoqueIntegracaoGateway.ItemDeducaoEstoque> itens = List.of(
                new EstoqueIntegracaoGateway.ItemDeducaoEstoque(INSUMO_ID, 2));
        when(internalServiceTokenProvider.obterAuthorizationHeader()).thenReturn(AUTHORIZATION_HEADER);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/estoque/deduzir")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(List.of(new DeduzirEstoqueItemDto(INSUMO_ID, 2)))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        assertThatCode(() -> estoqueIntegracaoAdapter.deduzirEstoque(itens)).doesNotThrowAnyException();

        verify(responseSpec).toBodilessEntity();
    }

    @Test
    void deveMapearErroDeEstoqueInsuficiente() {
        List<EstoqueIntegracaoGateway.ItemDeducaoEstoque> itens = List.of(
                new EstoqueIntegracaoGateway.ItemDeducaoEstoque(INSUMO_ID, 20));
        String resposta = "{\"erros\":[{\"codigo\":\"estoque-insuficiente\","
                + "\"descricao\":\"Estoque insuficiente para o insumo 'Filtro de oleo'.\"}]}";

        when(internalServiceTokenProvider.obterAuthorizationHeader()).thenReturn(AUTHORIZATION_HEADER);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/estoque/deduzir")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(List.of(new DeduzirEstoqueItemDto(INSUMO_ID, 20)))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpHeaders.EMPTY,
                resposta.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8));

        assertThatThrownBy(() -> estoqueIntegracaoAdapter.deduzirEstoque(itens))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("Filtro de oleo");
    }

    private HttpClientErrorException criarHttpClientErrorException(HttpStatus status) {
        return HttpClientErrorException.create(status, status.getReasonPhrase(), HttpHeaders.EMPTY, new byte[0], null);
    }
}
