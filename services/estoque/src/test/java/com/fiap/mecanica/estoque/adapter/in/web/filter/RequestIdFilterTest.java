package com.fiap.mecanica.estoque.adapter.in.web.filter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.KeyValuePair;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();
    private final Logger logger = (Logger) LoggerFactory.getLogger(RequestIdFilter.class);
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void prepararAppender() {
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void removerAppender() {
        logger.detachAppender(appender);
    }

    @Test
    void deveReaproveitarRequestIdRecebidoNoHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER, "abc-123_X.9");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> noMdc = new AtomicReference<>();
        FilterChain chain = (req, res) -> noMdc.set(MDC.get(RequestIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        assertThat(noMdc.get()).isEqualTo("abc-123_X.9");
        assertThat(response.getHeader(RequestIdFilter.HEADER)).isEqualTo("abc-123_X.9");
    }

    @Test
    void deveGerarRequestIdQuandoHeaderAusente() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> noMdc = new AtomicReference<>();
        FilterChain chain = (req, res) -> noMdc.set(MDC.get(RequestIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        assertThat(noMdc.get()).isNotBlank();
        assertThat(response.getHeader(RequestIdFilter.HEADER)).isEqualTo(noMdc.get());
    }

    @Test
    void deveSubstituirRequestIdComFormatoInvalido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER, "invalido\r\nX-Injetado: 1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> { };

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(RequestIdFilter.HEADER)).doesNotContain("Injetado").hasSize(36);
    }

    @Test
    void deveLimparMdcAoFinalMesmoQuandoACadeiaFalha() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER, "falha-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            throw new IllegalStateException("erro");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(IllegalStateException.class);

        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void deveRegistrarRequisicaoComRotaStatusEDuracao() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/recurso/10");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/recurso/{id}");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((MockHttpServletResponse) res).setStatus(404);

        filter.doFilter(request, response, chain);

        assertThat(appender.list).hasSize(1);
        Map<String, Object> campos = camposDe(appender.list.getFirst());
        assertThat(appender.list.getFirst().getFormattedMessage()).isEqualTo("Requisição concluída");
        assertThat(campos).containsEntry("metodo", "GET")
                .containsEntry("rota", "/recurso/{id}")
                .containsEntry("status", 404)
                .containsKey("duracao_ms");
    }

    @Test
    void deveRegistrarRotaDesconhecidaQuandoNaoHaPadraoMapeado() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/inexistente");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> { };

        filter.doFilter(request, response, chain);

        assertThat(camposDe(appender.list.getFirst())).containsEntry("rota", "desconhecida");
    }

    @Test
    void naoDeveRegistrarRequisicoesDoActuator() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health/liveness");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> { };

        filter.doFilter(request, response, chain);

        assertThat(appender.list).isEmpty();
    }

    private Map<String, Object> camposDe(ILoggingEvent evento) {
        List<KeyValuePair> pares = evento.getKeyValuePairs();
        return pares.stream().collect(Collectors.toMap(par -> par.key, par -> par.value));
    }
}
