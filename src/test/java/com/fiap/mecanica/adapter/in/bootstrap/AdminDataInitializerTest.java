package com.fiap.mecanica.adapter.in.bootstrap;

import com.fiap.mecanica.application.port.in.FuncionarioUseCase;
import com.fiap.mecanica.domain.Funcao;
import com.fiap.mecanica.domain.Funcionario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDataInitializerTest {

    @Mock
    private FuncionarioUseCase funcionarioUseCase;

    private AdminDataInitializer adminDataInitializer;

    private final String adminEmail = "admin@test.com";
    private final String adminPassword = "password123";
    private final String adminNome = "Admin Test";

    @BeforeEach
    void setUp() {
        adminDataInitializer = new AdminDataInitializer(
                funcionarioUseCase,
                adminEmail,
                adminPassword,
                adminNome);
    }

    @Test
    void deveCriarAdminPadraoQuandoNaoExistir() {
        when(funcionarioUseCase.buscarFuncionarioPorEmail(adminEmail)).thenReturn(Optional.empty());

        adminDataInitializer.run();

        ArgumentCaptor<Funcionario> captor = ArgumentCaptor.forClass(Funcionario.class);
        verify(funcionarioUseCase).cadastrarFuncionario(captor.capture());

        Funcionario adminCriado = captor.getValue();
        assertThat(adminCriado.getEmail()).isEqualTo(adminEmail);
        assertThat(adminCriado.getSenha()).isEqualTo(adminPassword);
        assertThat(adminCriado.getFuncao()).isEqualTo(Funcao.ADMIN);
        assertThat(adminCriado.getNome()).isEqualTo(adminNome);
        assertThat(adminCriado.isAtivo()).isTrue();
    }

    @Test
    void naoDeveCriarAdminPadraoQuandoJaExistir() {
        when(funcionarioUseCase.buscarFuncionarioPorEmail(adminEmail))
                .thenReturn(Optional.of(new Funcionario()));

        adminDataInitializer.run();

        verify(funcionarioUseCase, never()).cadastrarFuncionario(any());
    }
}
