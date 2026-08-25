package com.fiap.mecanica.atendimento.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Schema(description = "Orçamento da ordem de serviço")
public record OrcamentoDto(
        @Schema(description = "Serviços do orçamento") List<OrdemServicoServicoDto> servicos,
        @Schema(description = "Insumos do orçamento") List<OrdemServicoInsumoDto> insumos,
        @Schema(description = "Preço total do orçamento", example = "300.00") BigDecimal precoTotal
) {
}
