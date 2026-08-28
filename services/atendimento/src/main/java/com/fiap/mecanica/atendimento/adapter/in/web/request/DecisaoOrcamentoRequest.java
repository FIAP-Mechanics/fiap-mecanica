package com.fiap.mecanica.atendimento.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Decisão externa do cliente sobre o orçamento de uma ordem de serviço")
public record DecisaoOrcamentoRequest(
        @NotNull
        @Schema(description = "Indica se o orçamento foi aprovado (true) ou recusado (false)", example = "true")
        Boolean aprovado
) {
}
