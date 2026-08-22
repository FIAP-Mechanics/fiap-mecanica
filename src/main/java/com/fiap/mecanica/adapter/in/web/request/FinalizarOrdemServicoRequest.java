package com.fiap.mecanica.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "Dados para finalizar a ordem de servico")
public record FinalizarOrdemServicoRequest(
        @NotEmpty(message = "Informe o tempo gasto nos servicos da ordem de servico") @Valid
        List<ServicoTempo> servicos) {

    @Schema(description = "Tempo gasto em um servico da ordem de servico")
    public record ServicoTempo(
            @NotNull(message = "O ID do servico e obrigatorio")
            @Schema(description = "ID do servico", example = "1") Long servico,
            @NotNull(message = "O tempo gasto e obrigatorio")
            @Positive(message = "O tempo gasto deve ser maior que zero")
            @Schema(description = "Tempo gasto em minutos", example = "90") Long tempoGastoMinutos) {
    }
}
