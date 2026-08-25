package com.fiap.mecanica.funcionario.adapter.out.security.gateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordEncoderSecurityGatewayTest {

    private static final String SENHA_CRUA = "senha123";
    private static final String SENHA_CODIFICADA = "senha-codificada";

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordEncoderSecurityGateway gateway;

    @Test
    void deveDelegarCodificacaoParaOPasswordEncoder() {
        when(passwordEncoder.encode(SENHA_CRUA)).thenReturn(SENHA_CODIFICADA);

        String resultado = gateway.encode(SENHA_CRUA);

        assertThat(resultado).isEqualTo(SENHA_CODIFICADA);
        verify(passwordEncoder).encode(SENHA_CRUA);
    }
}
