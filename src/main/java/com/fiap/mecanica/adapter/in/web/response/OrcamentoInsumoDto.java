package com.fiap.mecanica.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Dados de insumo vinculado ao orçamento")
public record OrcamentoInsumoDto(
        @Schema(description = "Identificador do insumo", example = "1") Long insumoId,
        @Schema(description = "Nome do insumo", example = "Óleo de motor") String nome,
        @Schema(description = "Preço unitário do insumo", example = "45.90") BigDecimal precoUnitario,
        @Schema(description = "Quantidade utilizada", example = "2") Integer quantidade,
        @Schema(description = "Valor total do insumo (preço unitário × quantidade)", example = "91.80") BigDecimal valorTotal) {
}
