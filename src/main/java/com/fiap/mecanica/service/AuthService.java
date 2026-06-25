package com.fiap.mecanica.service;

import com.fiap.mecanica.controller.request.AutenticarFuncionarioRequest;
import com.fiap.mecanica.dto.TokenDto;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public TokenDto autenticar(AutenticarFuncionarioRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        return jwtTokenService.gerarToken(authentication);
    }
}
