package com.fiap.mecanica.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Dados de um serviço vinculado à ordem de serviço")
public record OrdemServicoServicoDto(
        @Schema(description = "ID do serviço", example = "1") Long servicoId,
        @Schema(description = "Nome do serviço", example = "Troca de óleo") String nome,
        @Schema(description = "Valor unitário do serviço", example = "150.00") BigDecimal valorUnitario,
        @Schema(description = "Quantidade solicitada", example = "1") Integer quantidade,
        @Schema(description = "Tempo gasto na execução do serviço em minutos", example = "90") Long tempoExecucaoMinutos,
        @Schema(description = "Valor total do serviço", example = "150.00") BigDecimal valorTotal) {
}
