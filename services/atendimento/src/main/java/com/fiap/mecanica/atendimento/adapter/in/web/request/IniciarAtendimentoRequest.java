package com.fiap.mecanica.atendimento.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

@Schema(description = "Dados para iniciar um atendimento")
public record IniciarAtendimentoRequest(
        @NotNull(message = "O ID do cliente é obrigatório")
        @Schema(description = "ID do cliente", example = "1")
        Long cliente,

        @NotNull(message = "O ID do veículo é obrigatório")
        @Schema(description = "ID do veículo", example = "1")
        Long veiculo,

        @NotBlank(message = "O relato do cliente é obrigatório")
        @Schema(description = "Relato do cliente sobre os problemas a serem verificados", example = "Barulho na suspensão dianteira ao passar por buracos")
        String relatoCliente,

        @Schema(description = "Lista de serviços requeridos com ID e quantidade")
        List<ServicoQuantidade> servicos,

        @Schema(description = "Lista de insumos requeridos com ID e quantidade")
        List<InsumoQuantidade> insumos
) {
    @Schema(description = "Dados de serviço com quantidade")
    public record ServicoQuantidade(
            @NotNull(message = "O ID do serviço é obrigatório")
            @Schema(description = "ID do serviço", example = "1")
            Long servico,

            @NotNull(message = "A quantidade é obrigatória")
            @PositiveOrZero(message = "A quantidade deve ser maior ou igual a zero")
            @Schema(description = "Quantidade do serviço", example = "2")
            Integer quantidade
    ) {
    }

    @Schema(description = "Dados de insumo com quantidade")
    public record InsumoQuantidade(
            @NotNull(message = "O ID do insumo é obrigatório")
            @Schema(description = "ID do insumo", example = "1")
            Long insumo,

            @NotNull(message = "A quantidade é obrigatória")
            @PositiveOrZero(message = "A quantidade deve ser maior ou igual a zero")
            @Schema(description = "Quantidade do insumo", example = "2")
            Integer quantidade
    ) {
    }
}
