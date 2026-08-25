package com.fiap.mecanica.estoque.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Dados para atualizar a quantidade em estoque")
public record AtualizarEstoqueRequest(
        @NotNull @PositiveOrZero @Schema(description = "Nova quantidade", example = "15") Long quantidade) {
}
