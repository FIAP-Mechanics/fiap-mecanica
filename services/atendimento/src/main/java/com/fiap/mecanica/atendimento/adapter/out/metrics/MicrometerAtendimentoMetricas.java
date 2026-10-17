package com.fiap.mecanica.atendimento.adapter.out.metrics;

import com.fiap.mecanica.atendimento.application.port.out.AtendimentoMetricasGateway;
import com.fiap.mecanica.atendimento.domain.Status;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MicrometerAtendimentoMetricas implements AtendimentoMetricasGateway {

    private final MeterRegistry registry;
    private final Counter ordensCriadas;
    private final Counter falhasNotificacao;

    public MicrometerAtendimentoMetricas(MeterRegistry registry) {
        this.registry = registry;
        this.ordensCriadas = Counter.builder("os.criadas")
                .description("Ordens de serviço abertas")
                .register(registry);
        this.falhasNotificacao = Counter.builder("notificacao.falhas")
                .description("Falhas no envio de notificações por e-mail")
                .register(registry);
    }

    @Override
    public void registrarOrdemCriada() {
        ordensCriadas.increment();
    }

    @Override
    public void registrarTransicao(Status statusAnterior, Status novoStatus, Duration permanenciaNoStatusAnterior) {
        Timer.builder("os.status.duracao")
                .description("Tempo de permanência da ordem de serviço em cada status")
                .tag("status", statusAnterior.name())
                .register(registry)
                .record(permanenciaNoStatusAnterior);
        Counter.builder("os.transicoes")
                .description("Transições de status das ordens de serviço")
                .tag("de", statusAnterior.name())
                .tag("para", novoStatus.name())
                .register(registry)
                .increment();
    }

    @Override
    public void registrarFalhaNotificacao() {
        falhasNotificacao.increment();
    }
}
