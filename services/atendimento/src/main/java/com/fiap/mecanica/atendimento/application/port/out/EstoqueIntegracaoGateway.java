package com.fiap.mecanica.atendimento.application.port.out;

import java.math.BigDecimal;
import java.util.List;

public interface EstoqueIntegracaoGateway {

    InsumoIntegracao buscarInsumo(Long insumoId);

    void deduzirEstoque(List<ItemDeducaoEstoque> itens);

    record InsumoIntegracao(Long id, String nome, BigDecimal precoUnitario) {
    }

    record ItemDeducaoEstoque(Long insumoId, Integer quantidade) {
    }
}
