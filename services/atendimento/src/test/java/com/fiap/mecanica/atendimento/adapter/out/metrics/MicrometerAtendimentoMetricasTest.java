package com.fiap.mecanica.atendimento.adapter.out.metrics;

import com.fiap.mecanica.atendimento.domain.Status;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class MicrometerAtendimentoMetricasTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final MicrometerAtendimentoMetricas metricas = new MicrometerAtendimentoMetricas(registry);

    @Test
    void deveIncrementarContadorDeOrdensCriadas() {
        metricas.registrarOrdemCriada();
        metricas.registrarOrdemCriada();

        assertThat(registry.get("os.criadas").counter().count()).isEqualTo(2.0);
    }

    @Test
    void deveRegistrarPermanenciaPorStatusAnteriorEContarATransicao() {
        metricas.registrarTransicao(Status.EM_DIAGNOSTICO, Status.AGUARDANDO_APROVACAO, Duration.ofMinutes(15));

        assertThat(registry.get("os.status.duracao").tag("status", "EM_DIAGNOSTICO").timer().totalTime(TimeUnit.MINUTES))
                .isEqualTo(15.0);
        assertThat(registry.get("os.transicoes").tag("de", "EM_DIAGNOSTICO").tag("para", "AGUARDANDO_APROVACAO")
                .counter().count()).isEqualTo(1.0);
    }

    @Test
    void deveAcumularMultiplasPermanenciasNoMesmoStatus() {
        metricas.registrarTransicao(Status.EM_EXECUCAO, Status.FINALIZADA, Duration.ofMinutes(30));
        metricas.registrarTransicao(Status.EM_EXECUCAO, Status.FINALIZADA, Duration.ofMinutes(90));

        var timer = registry.get("os.status.duracao").tag("status", "EM_EXECUCAO").timer();
        assertThat(timer.count()).isEqualTo(2);
        assertThat(timer.mean(TimeUnit.MINUTES)).isEqualTo(60.0);
    }

    @Test
    void deveIncrementarContadorDeFalhasDeNotificacao() {
        metricas.registrarFalhaNotificacao();

        assertThat(registry.get("notificacao.falhas").counter().count()).isEqualTo(1.0);
    }
}
