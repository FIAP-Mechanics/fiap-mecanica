package com.fiap.mecanica.estoque.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
@Schema(description = "Item para dedução de estoque")
public record DeduzirEstoqueItemDto(
        @NotNull @Schema(description = "ID do insumo", example = "1") Long insumoId,
        @NotNull @Positive @Schema(description = "Quantidade a deduzir", example = "2") Long quantidade
) {
}
