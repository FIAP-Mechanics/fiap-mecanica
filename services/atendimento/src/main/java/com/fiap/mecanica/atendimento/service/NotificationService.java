package com.fiap.mecanica.atendimento.service;

import com.fiap.mecanica.atendimento.client.dto.ClienteIntegracaoDto;
import com.fiap.mecanica.atendimento.infra.enums.CodigoTemplate;

public interface NotificationService {

    void notificarCliente(CodigoTemplate template, ClienteIntegracaoDto cliente, String... args);

    void notificarFuncionarios(CodigoTemplate template, String... args);
}
