package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AutenticarCommand;
import com.fiap.mecanica.application.port.out.AutenticacaoGateway;
import com.fiap.mecanica.application.port.out.GeradorTokenGateway;
import com.fiap.mecanica.application.result.IdentidadeAutenticadaResult;
import com.fiap.mecanica.application.result.TokenResult;
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
class AutenticacaoInteractorTest {

    @Mock
    private AutenticacaoGateway autenticacaoGateway;

    @Mock
    private GeradorTokenGateway geradorTokenGateway;

    @InjectMocks
    private AutenticacaoInteractor interactor;

    @Test
    void deveAutenticarFuncionarioEGerarToken() {
        AutenticarCommand command =
                new AutenticarCommand("admin@mecanica.com", "senha123");
        IdentidadeAutenticadaResult identidade =
                new IdentidadeAutenticadaResult("admin@mecanica.com", List.of("ROLE_ADMIN"));
        TokenResult token = new TokenResult("jwt", "Bearer", 3600L);
        when(autenticacaoGateway.autenticar(command.email(), command.senha()))
                .thenReturn(identidade);
        when(geradorTokenGateway.gerar(identidade)).thenReturn(token);

        TokenResult resultado = interactor.autenticar(command);

        assertThat(resultado).isEqualTo(token);
        verify(autenticacaoGateway).autenticar("admin@mecanica.com", "senha123");
        verify(geradorTokenGateway).gerar(identidade);
    }
}
