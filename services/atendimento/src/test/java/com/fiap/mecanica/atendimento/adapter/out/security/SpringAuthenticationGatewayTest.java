package com.fiap.mecanica.atendimento.adapter.out.security;

import com.fiap.mecanica.atendimento.application.port.out.AuthenticationGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringAuthenticationGatewayTest {

    private static final String EMAIL = "admin@mecanica.com";
    private static final String SENHA = "senha123";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SpringAuthenticationGateway springAuthenticationGateway;

    @Test
    void deveAutenticarERemoverPrefixoRoleDasAuthorities() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn(EMAIL);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

        AuthenticationGateway.AutenticacaoResultado resultado = springAuthenticationGateway.autenticar(EMAIL, SENHA);

        assertThat(resultado.subject()).isEqualTo(EMAIL);
        assertThat(resultado.roles()).containsExactly("ADMIN");

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo(EMAIL);
        assertThat(captor.getValue().getCredentials()).isEqualTo(SENHA);
    }
}
