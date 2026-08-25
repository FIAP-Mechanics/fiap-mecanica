package com.fiap.mecanica.atendimento.application.port.out;

import com.fiap.mecanica.atendimento.domain.CodigoTemplate;

public interface NotificationGateway {

    void notificarCliente(CodigoTemplate template, ClienteIntegracaoGateway.ClienteIntegracao cliente, String... args);

    void notificarFuncionarios(CodigoTemplate template, String... args);
}
