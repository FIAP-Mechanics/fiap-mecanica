package com.fiap.mecanica.estoque.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Dados para cadastro do insumo")
public record CadastrarInsumoRequest(
        @NotBlank @Schema(description = "Nome do insumo", example = "Óleo") String nome,
        @NotNull @PositiveOrZero @Digits(integer = 8, fraction = 2)
        @Schema(description = "Preço unitário", example = "45.90") BigDecimal precoUnitario
) {
}
