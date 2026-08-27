package com.fiap.mecanica.atendimento.adapter.out.client;

import com.fiap.mecanica.atendimento.adapter.out.client.dto.VeiculoIntegracaoDto;
import com.fiap.mecanica.atendimento.exception.VeiculoInativoException;
import com.fiap.mecanica.atendimento.exception.VeiculoNaoEncontradoException;
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class VeiculoIntegracaoAdapterTest {

    private static final Long VEICULO_ID = 1L;
    private static final String BASE_URL = "http://veiculo-service";
    private static final String AUTHORIZATION_HEADER = "Bearer token-interno";

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

    private VeiculoIntegracaoAdapter veiculoIntegracaoAdapter;

    @BeforeEach
    void configurarAdapter() {
        restClientMockedStatic = mockStatic(RestClient.class);
        restClientMockedStatic.when(() -> RestClient.create(BASE_URL)).thenReturn(restClient);
        veiculoIntegracaoAdapter = new VeiculoIntegracaoAdapter(BASE_URL, internalServiceTokenProvider);
    }

    @AfterEach
    void finalizarMockStatic() {
        restClientMockedStatic.close();
    }

    // ===================== buscarVeiculo =====================

    @Test
    void deveConcluirSemErroQuandoVeiculoExistirEEstiverAtivo() {
        configurarCadeiaRestClient();
        when(responseSpec.body(VeiculoIntegracaoDto.class))
                .thenReturn(new VeiculoIntegracaoDto(VEICULO_ID, "ABC-1234"));

        assertThatCode(() -> veiculoIntegracaoAdapter.buscarVeiculo(VEICULO_ID)).doesNotThrowAnyException();
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionQuandoVeiculoNaoExistir() {
        configurarCadeiaRestClient();
        when(responseSpec.body(VeiculoIntegracaoDto.class))
                .thenThrow(criarHttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> veiculoIntegracaoAdapter.buscarVeiculo(VEICULO_ID))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessageContaining(String.valueOf(VEICULO_ID));
    }

    @Test
    void deveLancarVeiculoInativoExceptionQuandoVeiculoEstiverInativo() {
        configurarCadeiaRestClient();
        when(responseSpec.body(VeiculoIntegracaoDto.class))
                .thenThrow(criarHttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> veiculoIntegracaoAdapter.buscarVeiculo(VEICULO_ID))
                .isInstanceOf(VeiculoInativoException.class)
                .hasMessageContaining(String.valueOf(VEICULO_ID));
    }

    private void configurarCadeiaRestClient() {
        when(internalServiceTokenProvider.obterAuthorizationHeader()).thenReturn(AUTHORIZATION_HEADER);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/veiculos/{id}", VEICULO_ID)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    private HttpClientErrorException criarHttpClientErrorException(HttpStatus status) {
        return HttpClientErrorException.create(status, status.getReasonPhrase(), HttpHeaders.EMPTY, new byte[0], null);
    }
}
