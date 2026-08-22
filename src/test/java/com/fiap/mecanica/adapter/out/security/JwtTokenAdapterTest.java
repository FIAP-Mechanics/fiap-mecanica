package com.fiap.mecanica.adapter.out.security;

import com.fiap.mecanica.application.result.IdentidadeAutenticadaResult;
import com.fiap.mecanica.application.result.TokenResult;
import com.fiap.mecanica.infrastructure.configuration.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenAdapterTest {

    private static final String SECRET =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void deveGerarJwtComFuncionarioERoles() {
        SecurityConfiguration configuration = new SecurityConfiguration();
        JwtEncoder encoder = configuration.jwtEncoder(SECRET);
        JwtDecoder decoder = configuration.jwtDecoder(SECRET);
        JwtTokenAdapter adapter = new JwtTokenAdapter(encoder, 600L, "http://mecanica-test");
        IdentidadeAutenticadaResult identidade = new IdentidadeAutenticadaResult(
                "admin@mecanica.com",
                List.of("ROLE_ADMIN"));

        TokenResult token = adapter.gerar(identidade);
        Jwt jwt = decoder.decode(token.accessToken());

        assertThat(token.tokenType()).isEqualTo("Bearer");
        assertThat(token.expiresIn()).isEqualTo(600L);
        assertThat(jwt.getSubject()).isEqualTo("admin@mecanica.com");
        assertThat(jwt.getIssuer().toString()).isEqualTo("http://mecanica-test");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ADMIN");
    }
}
