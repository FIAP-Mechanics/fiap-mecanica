package com.fiap.mecanica.dto;

import com.fiap.mecanica.domain.ServicoInsumo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Schema(description = "Dados do servico")
public record ServicoDto(
        @Schema(description = "Identificador unico do servico", example = "1") Long id,
        @Schema(description = "Nome do servico", example = "Troca de oleo") String nome,
        @Schema(description = "Descricao detalhada do servico") String descricao,
        @Schema(description = "Valor do servico", example = "150.00") BigDecimal valor,
        @Schema(description = "Insumos e quantidades utilizados no servico") List<ServicoInsumo> insumos) {
}
