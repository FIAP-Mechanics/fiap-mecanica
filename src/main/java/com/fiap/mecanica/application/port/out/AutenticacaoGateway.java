package com.fiap.mecanica.application.port.out;

import com.fiap.mecanica.application.result.IdentidadeAutenticadaResult;

public interface AutenticacaoGateway {
    IdentidadeAutenticadaResult autenticar(String email, String senha);
}
