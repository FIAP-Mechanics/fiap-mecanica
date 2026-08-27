package com.fiap.mecanica.atendimento.adapter.out.security;

import com.fiap.mecanica.atendimento.application.port.out.AuthenticationGateway;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@AllArgsConstructor
@Component
public class SpringAuthenticationGateway implements AuthenticationGateway {

    private final AuthenticationManager authenticationManager;

    @Override
    public AutenticacaoResultado autenticar(String email, String senha) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, senha));

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .toList();

        return new AutenticacaoResultado(authentication.getName(), roles);
    }
}
