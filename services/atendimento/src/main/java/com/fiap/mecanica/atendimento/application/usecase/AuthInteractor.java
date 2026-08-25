package com.fiap.mecanica.atendimento.application.usecase;

import com.fiap.mecanica.atendimento.application.port.in.AuthUseCase;
import com.fiap.mecanica.atendimento.application.port.out.AuthenticationGateway;
import com.fiap.mecanica.atendimento.application.port.out.TokenGateway;
import com.fiap.mecanica.atendimento.domain.Token;

public class AuthInteractor implements AuthUseCase {

    private final AuthenticationGateway authenticationGateway;
    private final TokenGateway tokenGateway;

    public AuthInteractor(AuthenticationGateway authenticationGateway, TokenGateway tokenGateway) {
        this.authenticationGateway = authenticationGateway;
        this.tokenGateway = tokenGateway;
    }

    @Override
    public Token autenticar(String email, String senha) {
        AuthenticationGateway.AutenticacaoResultado resultado = authenticationGateway.autenticar(email, senha);
        return tokenGateway.gerarToken(resultado.subject(), resultado.roles());
    }
}
