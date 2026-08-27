package com.fiap.mecanica.atendimento.adapter.out.security;

import com.fiap.mecanica.atendimento.domain.Token;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenGatewayTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void deveGerarJwtComSubjectERoles() {
        SecretKey secretKey = criarSecretKey(SECRET);
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
        JwtTokenGateway gateway = new JwtTokenGateway(encoder, 600L, "http://mecanica-test");

        Token token = gateway.gerarToken("admin@mecanica.com", List.of("ADMIN"));
        Jwt jwt = decoder.decode(token.getAccessToken());

        assertThat(token.getTokenType()).isEqualTo("Bearer");
        assertThat(token.getExpiresIn()).isEqualTo(600L);
        assertThat(jwt.getSubject()).isEqualTo("admin@mecanica.com");
        assertThat(jwt.getIssuer().toString()).isEqualTo("http://mecanica-test");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ADMIN");
    }

    private SecretKey criarSecretKey(String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
