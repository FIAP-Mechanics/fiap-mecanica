package com.fiap.mecanica.atendimento.application.port.in;

import com.fiap.mecanica.atendimento.domain.Token;

public interface AuthUseCase {

    Token autenticar(String email, String senha);
}
