package com.fiap.mecanica.atendimento.adapter.out.client;

import com.fiap.mecanica.atendimento.adapter.out.client.dto.ClienteIntegracaoDto;
import com.fiap.mecanica.atendimento.application.port.out.ClienteIntegracaoGateway;
import com.fiap.mecanica.atendimento.exception.ClienteNaoEncontradoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class ClienteIntegracaoAdapterTest {

    private static final Long CLIENTE_ID = 1L;
    private static final String BASE_URL = "http://cliente-service";
    private static final String AUTHORIZATION_HEADER = "Bearer token-interno";
    private static final String NOME_CLIENTE = "Fulano de Tal";
    private static final String DOCUMENTO_CLIENTE = "12345678900";
    private static final String EMAIL_CLIENTE = "fulano@email.com";

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

    private MockedStatic<RestClient> restClientMockedStatic;

    private ClienteIntegracaoAdapter clienteIntegracaoAdapter;

    @BeforeEach
    void configurarAdapter() {
        restClientMockedStatic = mockStatic(RestClient.class);
        restClientMockedStatic.when(() -> RestClient.create(BASE_URL)).thenReturn(restClient);
        clienteIntegracaoAdapter = new ClienteIntegracaoAdapter(BASE_URL, internalServiceTokenProvider);
    }

    @AfterEach
    void finalizarMockStatic() {
        restClientMockedStatic.close();
    }

    // ===================== buscarCliente =====================

    @Test
    void deveRetornarClienteIntegracaoQuandoBuscarComSucesso() {
        ClienteIntegracaoDto dto = new ClienteIntegracaoDto(CLIENTE_ID, NOME_CLIENTE, DOCUMENTO_CLIENTE, EMAIL_CLIENTE);
        configurarCadeiaRestClient();
        when(responseSpec.body(ClienteIntegracaoDto.class)).thenReturn(dto);

        ClienteIntegracaoGateway.ClienteIntegracao resultado = clienteIntegracaoAdapter.buscarCliente(CLIENTE_ID);

        assertThat(resultado.id()).isEqualTo(CLIENTE_ID);
        assertThat(resultado.nome()).isEqualTo(NOME_CLIENTE);
        assertThat(resultado.documento()).isEqualTo(DOCUMENTO_CLIENTE);
        assertThat(resultado.email()).isEqualTo(EMAIL_CLIENTE);
    }

    @Test
    void deveLancarClienteNaoEncontradoExceptionQuandoClienteNaoExistir() {
        configurarCadeiaRestClient();
        when(responseSpec.body(ClienteIntegracaoDto.class))
                .thenThrow(criarHttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> clienteIntegracaoAdapter.buscarCliente(CLIENTE_ID))
                .isInstanceOf(ClienteNaoEncontradoException.class)
                .hasMessageContaining(String.valueOf(CLIENTE_ID));
    }

    private void configurarCadeiaRestClient() {
        when(internalServiceTokenProvider.obterAuthorizationHeader()).thenReturn(AUTHORIZATION_HEADER);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/cliente/{id}", CLIENTE_ID)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    private HttpClientErrorException criarHttpClientErrorException(HttpStatus status) {
        return HttpClientErrorException.create(status, status.getReasonPhrase(), HttpHeaders.EMPTY, new byte[0], null);
    }
}
