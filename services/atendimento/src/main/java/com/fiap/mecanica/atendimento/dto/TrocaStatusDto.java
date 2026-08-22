package com.fiap.mecanica.atendimento.dto;

import com.fiap.mecanica.atendimento.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Evento de troca de status da ordem de serviço")
public record TrocaStatusDto(
        @Schema(description = "Novo status") Status status,
        @Schema(description = "Data e hora da troca de status") LocalDateTime dataHora
) {
}
