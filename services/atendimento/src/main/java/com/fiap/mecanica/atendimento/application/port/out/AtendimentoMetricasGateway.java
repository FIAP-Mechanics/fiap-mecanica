package com.fiap.mecanica.atendimento.application.port.out;

import com.fiap.mecanica.atendimento.domain.Status;

import java.time.Duration;

public interface AtendimentoMetricasGateway {

    void registrarOrdemCriada();

    void registrarTransicao(Status statusAnterior, Status novoStatus, Duration permanenciaNoStatusAnterior);

    void registrarFalhaNotificacao();
}
