package com.fiap.mecanica.estoque.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Dados do insumo")
public record InsumoDto(
        @Schema(description = "Identificador único do insumo", example = "1") Long id,
        @Schema(description = "Nome do insumo", example = "Óleo") String nome,
        @Schema(description = "Preço unitário do insumo", example = "45.90") BigDecimal precoUnitario
) {
}
