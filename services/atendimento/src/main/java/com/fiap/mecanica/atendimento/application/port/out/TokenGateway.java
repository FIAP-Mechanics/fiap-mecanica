package com.fiap.mecanica.atendimento.application.port.out;

import com.fiap.mecanica.atendimento.domain.Token;

import java.util.List;

public interface TokenGateway {

    Token gerarToken(String subject, List<String> roles);
}
