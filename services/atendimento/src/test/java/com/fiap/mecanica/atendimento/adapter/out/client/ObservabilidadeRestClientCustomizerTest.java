package com.fiap.mecanica.atendimento.adapter.out.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ObservabilidadeRestClientCustomizerTest {

    private final ObservabilidadeRestClientCustomizer customizer = new ObservabilidadeRestClientCustomizer();
    private final Logger logger = (Logger) LoggerFactory.getLogger(ObservabilidadeRestClientCustomizer.class);
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void prepararAppender() {
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void limpar() {
        logger.detachAppender(appender);
        MDC.clear();
    }

    @Test
    void deveRegistrarInterceptorNoBuilder() {
        RestClient.Builder builder = mock(RestClient.Builder.class);

        customizer.customize(builder);

        verify(builder).requestInterceptor(customizer);
    }

    @Test
    void devePropagarRequestIdDoMdcParaRequisicaoDeSaida() throws IOException {
        MDC.put("request_id", "req-123");
        MockClientHttpRequest request = criarRequisicao();

        customizer.intercept(request, new byte[0], (req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        assertThat(request.getHeaders().getFirst("X-Request-ID")).isEqualTo("req-123");
    }

    @Test
    void naoDeveDefinirHeaderQuandoNaoHaRequestIdNoMdc() throws IOException {
        MockClientHttpRequest request = criarRequisicao();

        customizer.intercept(request, new byte[0], (req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        assertThat(request.getHeaders().containsHeader("X-Request-ID")).isFalse();
        assertThat(appender.list).isEmpty();
    }

    @Test
    void deveRegistrarAvisoQuandoIntegracaoRespondeComErro5xx() throws IOException {
        MockClientHttpRequest request = criarRequisicao();

        var resposta = customizer.intercept(request, new byte[0],
                (req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.SERVICE_UNAVAILABLE));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.getFirst().getLevel()).isEqualTo(Level.WARN);
        assertThat(appender.list.getFirst().getFormattedMessage())
                .contains("servico=estoque-service", "caminho=/estoque/1", "status=503");
    }

    @Test
    void naoDeveRegistrarAvisoParaErroDeCliente4xx() throws IOException {
        MockClientHttpRequest request = criarRequisicao();

        customizer.intercept(request, new byte[0], (req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.NOT_FOUND));

        assertThat(appender.list).isEmpty();
    }

    @Test
    void deveRegistrarErroEPropagarQuandoHaFalhaDeComunicacao() {
        MockClientHttpRequest request = criarRequisicao();

        assertThatThrownBy(() -> customizer.intercept(request, new byte[0], (req, body) -> {
            throw new IOException("connection refused");
        })).isInstanceOf(IOException.class);

        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(appender.list.getFirst().getFormattedMessage()).contains("servico=estoque-service");
    }

    private MockClientHttpRequest criarRequisicao() {
        return new MockClientHttpRequest(HttpMethod.GET, URI.create("http://estoque-service:8085/estoque/1"));
    }
}
