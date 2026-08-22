package com.fiap.mecanica.atendimento.service;

import com.fiap.mecanica.atendimento.config.SecurityConfig;
import com.fiap.mecanica.atendimento.dto.TokenDto;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void deveGerarJwtComFuncionarioERoles() {
        SecurityConfig config = new SecurityConfig();
        JwtEncoder encoder = config.jwtEncoder(SECRET);
        JwtDecoder decoder = config.jwtDecoder(SECRET);
        JwtTokenService service = new JwtTokenService(encoder, 600L, "http://mecanica-test");
        var authentication = new UsernamePasswordAuthenticationToken(
                "admin@mecanica.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        TokenDto token = service.gerarToken(authentication);
        Jwt jwt = decoder.decode(token.accessToken());

        assertThat(token.tokenType()).isEqualTo("Bearer");
        assertThat(token.expiresIn()).isEqualTo(600L);
        assertThat(jwt.getSubject()).isEqualTo("admin@mecanica.com");
        assertThat(jwt.getIssuer().toString()).isEqualTo("http://mecanica-test");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ADMIN");
    }
}
