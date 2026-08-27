package com.fiap.mecanica.atendimento.adapter.out.client;

import com.fiap.mecanica.atendimento.adapter.out.client.dto.ServicoIntegracaoDto;
import com.fiap.mecanica.atendimento.application.port.out.ServicoIntegracaoGateway;
import com.fiap.mecanica.atendimento.exception.ServicoNaoEncontradoException;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class ServicoIntegracaoAdapterTest {

    private static final Long SERVICO_ID = 1L;
    private static final String BASE_URL = "http://servico-service";
    private static final String AUTHORIZATION_HEADER = "Bearer token-interno";
    private static final String NOME_SERVICO = "Troca de oleo";
    private static final BigDecimal VALOR_SERVICO = BigDecimal.valueOf(150);

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

    private ServicoIntegracaoAdapter servicoIntegracaoAdapter;

    @BeforeEach
    void configurarAdapter() {
        restClientMockedStatic = mockStatic(RestClient.class);
        restClientMockedStatic.when(() -> RestClient.create(BASE_URL)).thenReturn(restClient);
        servicoIntegracaoAdapter = new ServicoIntegracaoAdapter(BASE_URL, internalServiceTokenProvider);
    }

    @AfterEach
    void finalizarMockStatic() {
        restClientMockedStatic.close();
    }

    // ===================== buscarServico =====================

    @Test
    void deveRetornarServicoIntegracaoQuandoBuscarComSucesso() {
        ServicoIntegracaoDto dto = new ServicoIntegracaoDto(SERVICO_ID, NOME_SERVICO, VALOR_SERVICO);
        configurarCadeiaRestClient();
        when(responseSpec.body(ServicoIntegracaoDto.class)).thenReturn(dto);

        ServicoIntegracaoGateway.ServicoIntegracao resultado = servicoIntegracaoAdapter.buscarServico(SERVICO_ID);

        assertThat(resultado.id()).isEqualTo(SERVICO_ID);
        assertThat(resultado.nome()).isEqualTo(NOME_SERVICO);
        assertThat(resultado.valor()).isEqualTo(VALOR_SERVICO);
    }

    @Test
    void deveLancarServicoNaoEncontradoExceptionQuandoServicoNaoExistir() {
        configurarCadeiaRestClient();
        when(responseSpec.body(ServicoIntegracaoDto.class))
                .thenThrow(criarHttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> servicoIntegracaoAdapter.buscarServico(SERVICO_ID))
                .isInstanceOf(ServicoNaoEncontradoException.class)
                .hasMessageContaining(String.valueOf(SERVICO_ID));
    }

    private void configurarCadeiaRestClient() {
        when(internalServiceTokenProvider.obterAuthorizationHeader()).thenReturn(AUTHORIZATION_HEADER);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/servicos/{id}", SERVICO_ID)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    private HttpClientErrorException criarHttpClientErrorException(HttpStatus status) {
        return HttpClientErrorException.create(status, status.getReasonPhrase(), HttpHeaders.EMPTY, new byte[0], null);
    }
}
