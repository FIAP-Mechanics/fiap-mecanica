package com.fiap.mecanica.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(description = "Dados para atualizacao de insumo do estoque")
public record AtualizarInsumoRequest(
        @Pattern(regexp = ".*\\S.*")
        @Schema(description = "Novo nome do insumo", example = "Oleo") String nome,
        @PositiveOrZero @Digits(integer = 8, fraction = 2)
        @Schema(description = "Novo preco do insumo", example = "45.90") BigDecimal precoUnitario) {
}
