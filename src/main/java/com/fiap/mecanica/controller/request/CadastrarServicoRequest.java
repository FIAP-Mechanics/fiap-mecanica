package com.fiap.mecanica.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Dados para cadastro de servico")
public record CadastrarServicoRequest(
        @NotBlank @Schema(description = "Tipo do servico", example = "Troca de oleo") String tipo,
        @Schema(description = "Descricao detalhada do servico") String descricao,
        @NotNull @PositiveOrZero @Schema(description = "Valor do servico", example = "150.00") BigDecimal valor) {
}
