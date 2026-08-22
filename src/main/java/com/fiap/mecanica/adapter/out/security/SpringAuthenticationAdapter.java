package com.fiap.mecanica.adapter.out.security;

import com.fiap.mecanica.application.port.out.AutenticacaoGateway;
import com.fiap.mecanica.application.result.IdentidadeAutenticadaResult;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SpringAuthenticationAdapter implements AutenticacaoGateway {

    private final AuthenticationManager authenticationManager;

    public SpringAuthenticationAdapter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public IdentidadeAutenticadaResult autenticar(String email, String senha) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, senha));

        return new IdentidadeAutenticadaResult(
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .toList()
        );
    }
}
