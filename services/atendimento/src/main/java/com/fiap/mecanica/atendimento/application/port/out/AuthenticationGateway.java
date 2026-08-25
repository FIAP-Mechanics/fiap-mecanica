package com.fiap.mecanica.atendimento.application.port.out;

import java.util.List;

public interface AuthenticationGateway {

    AutenticacaoResultado autenticar(String email, String senha);

    record AutenticacaoResultado(String subject, List<String> roles) {
    }
}
