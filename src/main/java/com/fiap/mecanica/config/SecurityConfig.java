package com.fiap.mecanica.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/error",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/funcionarios/**").hasRole("ADMIN")
                        .requestMatchers("/templates/**").hasRole("ADMIN")
                        .requestMatchers("/cliente/**", "/clientes/**", "/veiculos/**")
                        .hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.POST, "/atendimento/iniciar")
                        .hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.GET, "/atendimento/**")
                        .hasAnyRole("ADMIN", "ATENDENTE", "MECANICO")
                        .requestMatchers(HttpMethod.PATCH, "/atendimento/*/diagnostico/iniciar")
                        .hasAnyRole("ADMIN", "MECANICO")
                        .requestMatchers(HttpMethod.POST, "/atendimento/*/diagnostico")
                        .hasAnyRole("ADMIN", "MECANICO")
                        .requestMatchers(HttpMethod.POST, "/atendimento/*/finalizar")
                        .hasAnyRole("ADMIN", "MECANICO")
                        .requestMatchers(HttpMethod.POST, "/atendimento/*/aprovar")
                        .hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.POST, "/atendimento/*/cancelar")
                        .hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.POST, "/atendimento/*/entregar")
                        .hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers("/atendimento/**").hasRole("ADMIN")
                        .requestMatchers("/servicos/**").hasAnyRole("ADMIN", "MECANICO")
                        .requestMatchers("/estoque/**").hasAnyRole("ADMIN", "ATENDENTE", "MECANICO")
                        .anyRequest().hasRole("ADMIN"))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtEncoder jwtEncoder(@Value("${security.jwt.secret}") String secret) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey(secret)));
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey(secret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    private SecretKey jwtSecretKey(String secret) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secret.isBlank() || secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET deve ter pelo menos 32 bytes.");
        }
        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }
}
