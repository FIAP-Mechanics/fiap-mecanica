package com.fiap.mecanica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Dados do servico")
public record ServicoDto(
        @Schema(description = "Identificador unico do servico", example = "1") Long id,
        @Schema(description = "Nome do servico", example = "Troca de oleo") String nome,
        @Schema(description = "Descricao detalhada do servico") String descricao,
        @Schema(description = "Valor do servico", example = "150.00") BigDecimal valor) {
}
