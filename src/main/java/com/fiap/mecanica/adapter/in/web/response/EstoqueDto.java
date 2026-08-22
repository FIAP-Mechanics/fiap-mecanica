package com.fiap.mecanica.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Registro de insumo no estoque")
public record EstoqueDto(
        @Schema(description = "Dados do insumo") InsumoDto insumo,
        @Schema(description = "Quantidade do insumo disponivel", example = "10") Long quantidadeInsumo) {
}
