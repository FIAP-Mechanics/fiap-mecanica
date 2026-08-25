package com.fiap.mecanica.atendimento.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Item de insumo do orçamento")
public record OrdemServicoInsumoDto(
        @Schema(description = "ID do insumo", example = "1") Long insumoId,
        @Schema(description = "Nome do insumo", example = "Óleo 5W30") String nomeInsumo,
        @Schema(description = "Quantidade", example = "2") Integer quantidade,
        @Schema(description = "Preço unitário do insumo", example = "45.00") BigDecimal precoUnitario
) {
}
