package com.fiap.mecanica.adapter.in.web.presenter;

import com.fiap.mecanica.adapter.in.web.request.AutenticarFuncionarioRequest;
import com.fiap.mecanica.adapter.in.web.response.TokenDto;
import com.fiap.mecanica.application.command.AutenticarCommand;
import com.fiap.mecanica.application.result.TokenResult;

public final class AuthPresenter {

    private AuthPresenter() {
    }

    public static AutenticarCommand toCommand(AutenticarFuncionarioRequest request) {
        return new AutenticarCommand(request.email(), request.senha());
    }

    public static TokenDto toDto(TokenResult result) {
        return new TokenDto(result.accessToken(), result.tokenType(), result.expiresIn());
    }
}
