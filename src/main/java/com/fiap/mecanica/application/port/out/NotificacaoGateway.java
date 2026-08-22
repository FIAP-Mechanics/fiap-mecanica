package com.fiap.mecanica.application.port.out;

import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.CodigoTemplate;

public interface NotificacaoGateway {
    void notificarCliente(CodigoTemplate template, Cliente cliente, String... argumentos);
    void notificarFuncionarios(CodigoTemplate template, String... argumentos);
}
