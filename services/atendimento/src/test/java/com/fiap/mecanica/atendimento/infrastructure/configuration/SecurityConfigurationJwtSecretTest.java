package com.fiap.mecanica.atendimento.infrastructure.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityConfigurationJwtSecretTest {

    @Test
    void deveRejeitarSecretJwtFraca() {
        SecurityConfiguration config = new SecurityConfiguration();

        assertThatThrownBy(() -> config.jwtEncoder("muito-curta"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT_SECRET deve ter pelo menos 32 bytes.");
    }

    @Test
    void deveRejeitarSecretJwtEmBranco() {
        SecurityConfiguration config = new SecurityConfiguration();

        assertThatThrownBy(() -> config.jwtEncoder("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT_SECRET deve ter pelo menos 32 bytes.");
    }
}
