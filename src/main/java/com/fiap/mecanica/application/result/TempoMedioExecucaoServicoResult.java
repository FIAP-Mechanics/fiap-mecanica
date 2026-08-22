package com.fiap.mecanica.application.result;

import java.math.BigDecimal;

public record TempoMedioExecucaoServicoResult(
        Long servicoId,
        String nome,
        Long ordensFinalizadas,
        BigDecimal tempoMedioExecucaoMinutos) {
}
