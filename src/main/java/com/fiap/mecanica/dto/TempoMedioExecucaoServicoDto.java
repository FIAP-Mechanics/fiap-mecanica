package com.fiap.mecanica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Indicador de tempo medio de execucao por servico")
public record TempoMedioExecucaoServicoDto(
        @Schema(description = "ID do servico", example = "1")
        Long servicoId,

        @Schema(description = "Nome do servico", example = "Troca de oleo")
        String nome,

        @Schema(description = "Quantidade de ordens finalizadas consideradas no calculo", example = "3")
        Long ordensFinalizadas,

        @Schema(description = "Tempo medio de execucao em minutos", example = "125.50")
        BigDecimal tempoMedioExecucaoMinutos
) {
}
