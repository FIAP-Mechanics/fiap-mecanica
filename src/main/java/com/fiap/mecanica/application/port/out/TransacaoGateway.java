package com.fiap.mecanica.application.port.out;

public interface TransacaoGateway {
    void executar(Runnable trabalho);
}
