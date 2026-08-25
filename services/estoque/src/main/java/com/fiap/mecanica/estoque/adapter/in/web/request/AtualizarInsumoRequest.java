package com.fiap.mecanica.estoque.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Dados para atualização de insumo do estoque")
public record AtualizarInsumoRequest(
        @Pattern(regexp = ".*\\S.*")
        @Schema(description = "Novo nome do insumo", example = "Óleo") String nome,
        @PositiveOrZero
        @Digits(integer = 8, fraction = 2)
        @Schema(description = "Novo preço do insumo", example = "45.90") BigDecimal precoUnitario
) {
}
