package com.fiap.mecanica.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

@Builder
@Schema(description = "Dados para registrar um insumo no estoque")
public record CadastrarEstoqueRequest(
        @Valid @NotNull @Schema(description = "Dados do insumo") CadastrarInsumoRequest insumo,
        @NotNull @PositiveOrZero @Schema(description = "Quantidade inicial", example = "10") Long quantidade) {
}
