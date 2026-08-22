package com.fiap.mecanica.atendimento.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class InternalServiceTokenProvider {

    private static final String SUBJECT = "atendimento-service";
    private static final long RENOVACAO_ANTECEDENCIA_SEGUNDOS = 30;

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;
    private final String issuer;

    private volatile String tokenCache;
    private volatile Instant expiracaoCache;

    public InternalServiceTokenProvider(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.expiration-seconds}") long expirationSeconds,
            @Value("${security.jwt.issuer}") String issuer) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
        this.issuer = issuer;
    }

    public synchronized String obterAuthorizationHeader() {
        Instant now = Instant.now();
        if (tokenCache == null || expiracaoCache == null
                || now.isAfter(expiracaoCache.minusSeconds(RENOVACAO_ANTECEDENCIA_SEGUNDOS))) {
            gerarNovoToken(now);
        }
        return "Bearer " + tokenCache;
    }

    private void gerarNovoToken(Instant now) {
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(SUBJECT)
                .claim("roles", List.of("ADMIN"))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        tokenCache = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        expiracaoCache = expiresAt;
    }
}
