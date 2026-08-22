package com.fiap.mecanica.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Dados para cadastro de servico")
public record CadastrarServicoRequest(
        @NotBlank @Schema(description = "Nome do servico", example = "Troca de oleo") String nome,
        @Schema(description = "Descricao detalhada do servico") String descricao,
        @NotNull @PositiveOrZero @Schema(description = "Valor do servico", example = "150.00") BigDecimal valor) {
}
