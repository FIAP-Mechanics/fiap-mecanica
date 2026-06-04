package com.fiap.mecanica.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Dados para atualizacao de servico (todos os campos sao opcionais)")
public record AtualizarServicoRequest(
        @Schema(description = "Novo tipo do servico", example = "Troca de oleo") String tipo,
        @Schema(description = "Nova descricao detalhada do servico") String descricao,
        @Schema(description = "Novo valor do servico", example = "150.00") BigDecimal valor) {
}
