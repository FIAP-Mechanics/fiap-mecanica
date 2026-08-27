package com.fiap.mecanica.atendimento.application.port.out;

import java.math.BigDecimal;

public interface ServicoIntegracaoGateway {

    ServicoIntegracao buscarServico(Long id);

    record ServicoIntegracao(Long id, String nome, BigDecimal valor) {
    }
}
