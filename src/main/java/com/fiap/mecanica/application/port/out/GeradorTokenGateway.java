package com.fiap.mecanica.application.port.out;

import com.fiap.mecanica.application.result.IdentidadeAutenticadaResult;
import com.fiap.mecanica.application.result.TokenResult;

public interface GeradorTokenGateway {
    TokenResult gerar(IdentidadeAutenticadaResult identidade);
}
