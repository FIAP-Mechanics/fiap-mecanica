package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.infra.configs.enums.CodigoTemplate;

public interface NotificationService {

    void notificarCliente(CodigoTemplate template, Cliente cliente, String... args);

    void notificarFuncionarios(CodigoTemplate template, String... args);
}
