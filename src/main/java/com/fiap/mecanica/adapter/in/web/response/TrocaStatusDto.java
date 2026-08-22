package com.fiap.mecanica.adapter.in.web.response;

import com.fiap.mecanica.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Evento de troca de status da ordem de serviço")
public record TrocaStatusDto(
        @Schema(description = "Status registrado no evento", example = "EM_EXECUCAO") Status status,
        @Schema(description = "Data e hora da troca de status", example = "2024-03-20T10:30:00") LocalDateTime dataHora) {
}
