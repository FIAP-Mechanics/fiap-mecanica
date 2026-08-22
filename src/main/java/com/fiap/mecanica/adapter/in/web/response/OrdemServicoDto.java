package com.fiap.mecanica.adapter.in.web.response;

import com.fiap.mecanica.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "Dados da ordem de serviço")
public record OrdemServicoDto(
        @Schema(description = "Identificador único da ordem de serviço", example = "550e8400-e29b-41d4-a716-446655440000") String id,
        @Schema(description = "Status atual da ordem de serviço", example = "RECEBIDA") Status status,
        @Schema(description = "Descrição do status atual", example = "Seu veículo foi recebido e em breve iniciaremos o diagnóstico!") String descricaoStatus,
        @Schema(description = "Dados do cliente") ClienteDto cliente,
        @Schema(description = "Dados do veículo") VeiculoDto veiculo,
        @Schema(description = "Relato do cliente sobre os problemas a serem verificados", example = "Barulho na suspensão dianteira ao passar por buracos") String relatoCliente,
        @Schema(description = "Observações do mecânico durante o diagnóstico", example = "Amortecedores dianteiros com vazamento") String observacoesDiagnostico,
        @Schema(description = "Orçamento da ordem de serviço") OrcamentoDto orcamento,
        @Schema(description = "Histórico de eventos de troca de status da ordem de serviço") List<TrocaStatusDto> historicoDeEventos) {
}
