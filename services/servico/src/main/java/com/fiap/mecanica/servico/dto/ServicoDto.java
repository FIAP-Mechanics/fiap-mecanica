package com.fiap.mecanica.servico.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Dados do serviço")
public record ServicoDto(
        @Schema(description = "Identificador único do serviço", example = "1") Long id,
        @Schema(description = "Nome do serviço", example = "Troca de óleo") String nome,
        @Schema(description = "Descrição detalhada do serviço") String descricao,
        @Schema(description = "Valor do serviço", example = "150.00") BigDecimal valor
) {
}
