package com.fiap.mecanica.atendimento.adapter.out.security;

import com.fiap.mecanica.atendimento.application.port.out.TokenGateway;
import com.fiap.mecanica.atendimento.domain.Token;
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
public class JwtTokenGateway implements TokenGateway {

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;
    private final String issuer;

    public JwtTokenGateway(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.expiration-seconds}") long expirationSeconds,
            @Value("${security.jwt.issuer}") String issuer) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
        this.issuer = issuer;
    }

    @Override
    public Token gerarToken(String subject, List<String> roles) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(subject)
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return Token.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(expirationSeconds)
                .build();
    }
}
