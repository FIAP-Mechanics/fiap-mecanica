package com.fiap.mecanica.atendimento.application.usecase;

import com.fiap.mecanica.atendimento.application.port.out.AuthenticationGateway;
import com.fiap.mecanica.atendimento.application.port.out.TokenGateway;
import com.fiap.mecanica.atendimento.domain.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthInteractorTest {

    private static final String EMAIL = "admin@mecanica.com";
    private static final String SENHA = "senha123";

    @Mock
    private AuthenticationGateway authenticationGateway;

    @Mock
    private TokenGateway tokenGateway;

    @InjectMocks
    private AuthInteractor authInteractor;

    @Test
    void deveAutenticarFuncionarioEGerarToken() {
        List<String> roles = List.of("ADMIN");
        AuthenticationGateway.AutenticacaoResultado resultado =
                new AuthenticationGateway.AutenticacaoResultado(EMAIL, roles);
        Token token = Token.builder().accessToken("jwt").tokenType("Bearer").expiresIn(3600L).build();

        when(authenticationGateway.autenticar(EMAIL, SENHA)).thenReturn(resultado);
        when(tokenGateway.gerarToken(EMAIL, roles)).thenReturn(token);

        Token tokenGerado = authInteractor.autenticar(EMAIL, SENHA);

        assertThat(tokenGerado).isEqualTo(token);
        verify(authenticationGateway).autenticar(EMAIL, SENHA);
        verify(tokenGateway).gerarToken(EMAIL, roles);
    }
}
