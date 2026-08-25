package com.fiap.mecanica.atendimento.application.port.out;

public interface ClienteIntegracaoGateway {

    ClienteIntegracao buscarCliente(Long id);

    record ClienteIntegracao(Long id, String nome, String documento, String email) {
    }
}
