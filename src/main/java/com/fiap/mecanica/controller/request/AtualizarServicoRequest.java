package com.fiap.mecanica.controller.request;

import com.fiap.mecanica.domain.ServicoInsumo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Dados para atualizacao de servico (todos os campos sao opcionais)")
public record AtualizarServicoRequest(
        @Schema(description = "Novo nome do servico", example = "Troca de oleo") String nome,
        @Schema(description = "Nova descricao detalhada do servico") String descricao,
        @Schema(description = "Novo valor do servico", example = "150.00") BigDecimal valor,
        @Schema(description = "Novos insumos e quantidades utilizados no servico") List<ServicoInsumo> insumos) {
}
