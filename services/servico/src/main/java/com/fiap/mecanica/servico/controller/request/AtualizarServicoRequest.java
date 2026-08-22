package com.fiap.mecanica.servico.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Dados para atualização de serviço (todos os campos são opcionais)")
public record AtualizarServicoRequest(
        @Schema(description = "Novo nome do serviço", example = "Troca de óleo") String nome,
        @Schema(description = "Nova descrição detalhada do serviço") String descricao,
        @Schema(description = "Novo valor do serviço", example = "150.00") BigDecimal valor
) {
}
