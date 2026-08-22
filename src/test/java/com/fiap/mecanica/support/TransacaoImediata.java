package com.fiap.mecanica.support;

import com.fiap.mecanica.application.port.out.TransacaoGateway;

public final class TransacaoImediata implements TransacaoGateway {

    @Override
    public void executar(Runnable trabalho) {
        trabalho.run();
    }
}
