package com.fiap.mecanica.funcionario.adapter.out.security.gateway;

import com.fiap.mecanica.funcionario.application.port.out.PasswordEncoderGateway;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class PasswordEncoderSecurityGateway implements PasswordEncoderGateway {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String encode(String senha) {
        return passwordEncoder.encode(senha);
    }
}
