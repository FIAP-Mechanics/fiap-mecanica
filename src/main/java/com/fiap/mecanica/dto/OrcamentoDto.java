package com.fiap.mecanica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Schema(description = "Dados do orçamento da ordem de serviço")
public record OrcamentoDto(
        @Schema(description = "Identificador único do orçamento", example = "1")
        Long id,

        @Schema(description = "Serviços vinculados ao orçamento")
        List<OrdemServicoServicoDto> servicos,

        @Schema(description = "Insumos vinculados ao orçamento")
        List<OrcamentoInsumoDto> insumos,

        @Schema(description = "Preço total do orçamento (soma de serviços e insumos)", example = "350.00")
        BigDecimal precoTotal
) {
}
