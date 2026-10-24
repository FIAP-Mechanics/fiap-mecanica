package com.fiap.mecanica.servico.infrastructure.configuration;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ObservabilidadeConfigurationTest {

    private final ObservationPredicate predicado = new ObservabilidadeConfiguration().ignorarRequisicoesDoActuator();

    @Test
    void deveIgnorarObservacaoDeRequisicoesDoActuator() {
        var contexto = contextoPara("/actuator/health/liveness");

        assertThat(predicado.test("http.server.requests", contexto)).isFalse();
    }

    @Test
    void deveManterObservacaoDeRequisicoesDeNegocio() {
        var contexto = contextoPara("/recurso/1");

        assertThat(predicado.test("http.server.requests", contexto)).isTrue();
    }

    @Test
    void deveManterObservacoesQueNaoSaoDeRequisicaoDoServidor() {
        assertThat(predicado.test("http.client.requests", new Observation.Context())).isTrue();
    }

    private ServerRequestObservationContext contextoPara(String caminho) {
        return new ServerRequestObservationContext(
                new MockHttpServletRequest("GET", caminho), new MockHttpServletResponse());
    }
}
