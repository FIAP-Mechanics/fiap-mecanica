package com.fiap.mecanica.atendimento.infra.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Código do template de notificação por e-mail")
public enum CodigoTemplate {
    @Schema(description = "Notifica o cliente para autorizar o orçamento")
    AUTORIZAR_ORCAMENTO,
    @Schema(description = "Notifica o cliente de que o veículo está pronto para retirada")
    RETIRAR_VEICULO,
    @Schema(description = "Notifica o cliente de que o veículo foi retirado")
    VEICULO_RETIRADO,
    @Schema(description = "Notifica funcionários sobre reposição de estoque")
    REPOSICAO_ESTOQUE
}
