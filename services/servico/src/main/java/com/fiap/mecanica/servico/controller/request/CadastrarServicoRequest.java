package com.fiap.mecanica.servico.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Dados para cadastro de serviço")
public record CadastrarServicoRequest(
        @NotBlank @Schema(description = "Nome do serviço", example = "Troca de óleo") String nome,
        @Schema(description = "Descrição detalhada do serviço") String descricao,
        @NotNull @PositiveOrZero @Schema(description = "Valor do serviço", example = "150.00") BigDecimal valor
) {
}
