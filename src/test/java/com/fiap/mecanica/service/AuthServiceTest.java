package com.fiap.mecanica.service;

import com.fiap.mecanica.controller.request.AutenticarFuncionarioRequest;
import com.fiap.mecanica.dto.TokenDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService service;

    @Test
    void deveAutenticarFuncionarioEGerarToken() {
        AutenticarFuncionarioRequest request = new AutenticarFuncionarioRequest("admin@mecanica.com", "senha123");
        TokenDto token = new TokenDto("jwt", "Bearer", 3600L);
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any())).thenReturn(authentication);
        when(jwtTokenService.gerarToken(authentication)).thenReturn(token);

        TokenDto resultado = service.autenticar(request);

        assertThat(resultado).isEqualTo(token);
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("admin@mecanica.com");
        assertThat(captor.getValue().getCredentials()).isEqualTo("senha123");
    }
}
