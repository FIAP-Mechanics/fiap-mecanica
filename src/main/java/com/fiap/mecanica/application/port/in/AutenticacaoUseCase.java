package com.fiap.mecanica.application.port.in;

import com.fiap.mecanica.application.command.AutenticarCommand;
import com.fiap.mecanica.application.result.TokenResult;

public interface AutenticacaoUseCase {
    TokenResult autenticar(AutenticarCommand command);
}
