package com.fiap.mecanica.atendimento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Tempo médio de execução de um serviço")
public record TempoMedioExecucaoServicoDto(
        @Schema(description = "ID do serviço", example = "1") Long servicoId,
        @Schema(description = "Nome do serviço", example = "Troca de óleo") String nome,
        @Schema(description = "Quantidade de ordens finalizadas", example = "10") long ordensFinalizadas,
        @Schema(description = "Tempo médio de execução em minutos", example = "45") Long tempoMedioExecucaoMinutos
) {
}
