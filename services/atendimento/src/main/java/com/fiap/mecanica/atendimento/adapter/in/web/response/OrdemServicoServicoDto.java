package com.fiap.mecanica.atendimento.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Item de serviço do orçamento")
public record OrdemServicoServicoDto(
        @Schema(description = "ID do serviço", example = "1") Long servicoId,
        @Schema(description = "Nome do serviço", example = "Troca de óleo") String nomeServico,
        @Schema(description = "Quantidade", example = "1") Integer quantidade,
        @Schema(description = "Valor unitário do serviço", example = "150.00") BigDecimal valorUnitario
) {
}
