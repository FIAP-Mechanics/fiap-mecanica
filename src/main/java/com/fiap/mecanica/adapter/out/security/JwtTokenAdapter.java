package com.fiap.mecanica.adapter.out.security;

import com.fiap.mecanica.application.port.out.GeradorTokenGateway;
import com.fiap.mecanica.application.result.IdentidadeAutenticadaResult;
import com.fiap.mecanica.application.result.TokenResult;
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
public class JwtTokenAdapter implements GeradorTokenGateway {

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;
    private final String issuer;

    public JwtTokenAdapter(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.expiration-seconds}") long expirationSeconds,
            @Value("${security.jwt.issuer}") String issuer) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
        this.issuer = issuer;
    }

    @Override
    public TokenResult gerar(IdentidadeAutenticadaResult identidade) {
        Instant now = Instant.now();
        List<String> roles = identidade.autoridades().stream()
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(identidade.principal())
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new TokenResult(token, "Bearer", expirationSeconds);
    }
}
