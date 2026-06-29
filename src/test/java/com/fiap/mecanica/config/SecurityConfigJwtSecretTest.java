package com.fiap.mecanica.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityConfigJwtSecretTest {

    @Test
    void deveRejeitarSecretJwtFraca() {
        SecurityConfig config = new SecurityConfig();

        assertThatThrownBy(() -> config.jwtEncoder("muito-curta"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT_SECRET deve ter pelo menos 32 bytes.");
    }
}
