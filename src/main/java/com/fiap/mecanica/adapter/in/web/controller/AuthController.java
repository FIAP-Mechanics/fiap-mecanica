package com.fiap.mecanica.adapter.in.web.controller;

import com.fiap.mecanica.adapter.in.web.presenter.AuthPresenter;
import com.fiap.mecanica.adapter.in.web.request.AutenticarFuncionarioRequest;
import com.fiap.mecanica.adapter.in.web.response.TokenDto;
import com.fiap.mecanica.application.port.in.AutenticacaoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacao", description = "Operacoes de autenticacao de funcionarios")
public class AuthController {

    private final AutenticacaoUseCase autenticacao;

    @Operation(summary = "Gerar token JWT para funcionario")
    @PostMapping("/login")
    public TokenDto login(@Valid @RequestBody AutenticarFuncionarioRequest request) {
        return AuthPresenter.toDto(autenticacao.autenticar(AuthPresenter.toCommand(request)));
    }
}
