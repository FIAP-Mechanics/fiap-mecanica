package com.fiap.mecanica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Dados do insumo")
public record InsumoDto(
        @Schema(description = "Identificador unico do insumo", example = "1") Long id,
        @Schema(description = "Nome do insumo", example = "Oleo") String nome,
        @Schema(description = "Preco unitario do insumo", example = "45.90") BigDecimal precoUnitario) {
}
