package com.fiap.mecanica.atendimento.adapter.in.web.response;

import com.fiap.mecanica.atendimento.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Status atual de uma ordem de serviço")
public record StatusOrdemServicoDto(
        @Schema(description = "Identificador único da ordem de serviço", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Status atual da ordem de serviço", example = "RECEBIDA")
        Status status,

        @Schema(description = "Descrição do status atual", example = "Seu veículo foi recebido e em breve iniciaremos o diagnóstico!")
        String descricaoStatus
) {
}
