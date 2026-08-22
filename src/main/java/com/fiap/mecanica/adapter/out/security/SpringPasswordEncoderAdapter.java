package com.fiap.mecanica.adapter.out.security;

import com.fiap.mecanica.application.port.out.CodificadorSenhaGateway;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SpringPasswordEncoderAdapter implements CodificadorSenhaGateway {

    private final PasswordEncoder passwordEncoder;

    public SpringPasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String codificar(String senha) {
        return passwordEncoder.encode(senha);
    }
}
