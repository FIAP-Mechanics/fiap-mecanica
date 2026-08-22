package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AutenticarCommand;
import com.fiap.mecanica.application.port.in.AutenticacaoUseCase;
import com.fiap.mecanica.application.port.out.AutenticacaoGateway;
import com.fiap.mecanica.application.port.out.GeradorTokenGateway;
import com.fiap.mecanica.application.result.IdentidadeAutenticadaResult;
import com.fiap.mecanica.application.result.TokenResult;

public class AutenticacaoInteractor implements AutenticacaoUseCase {

    private final AutenticacaoGateway autenticacaoGateway;
    private final GeradorTokenGateway geradorTokenGateway;

    public AutenticacaoInteractor(
            AutenticacaoGateway autenticacaoGateway,
            GeradorTokenGateway geradorTokenGateway) {
        this.autenticacaoGateway = autenticacaoGateway;
        this.geradorTokenGateway = geradorTokenGateway;
    }

    @Override
    public TokenResult autenticar(AutenticarCommand command) {
        IdentidadeAutenticadaResult identidade =
                autenticacaoGateway.autenticar(command.email(), command.senha());
        return geradorTokenGateway.gerar(identidade);
    }
}
