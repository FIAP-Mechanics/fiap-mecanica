package com.fiap.mecanica.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.List;

@Schema(description = "Dados para adicionar serviços e insumos ao orçamento")
public record AdicionarItensOrcamentoRequest(
        @Schema(description = "Lista de serviços a serem adicionados") @Valid
        List<IniciarAtendimentoRequest.ServicoQuantidade> servicos,
        @Schema(description = "Lista de insumos a serem adicionados") @Valid
        List<IniciarAtendimentoRequest.InsumoQuantidade> insumos,
        @Schema(description = "Observações do mecânico durante o diagnóstico", example = "Identificado vazamento de óleo na junta do cabeçote")
        String observacoes) {
}
