package com.fiap.mecanica.atendimento.adapter.in.bootstrap;

import com.fiap.mecanica.atendimento.application.port.out.FuncionarioGateway;
import com.fiap.mecanica.atendimento.domain.Funcao;
import com.fiap.mecanica.atendimento.domain.Funcionario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDataInitializerTest {

    private static final String SENHA_CRIPTOGRAFADA = "senha-criptografada";

    @Mock
    private FuncionarioGateway funcionarioGateway;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminDataInitializer adminDataInitializer;

    @Captor
    private ArgumentCaptor<Funcionario> funcionarioCaptor;

    // ===================== run =====================

    @Test
    void deveCriarFuncionarioAdminQuandoNaoExistir() {
        when(funcionarioGateway.buscarPorEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn(SENHA_CRIPTOGRAFADA);

        adminDataInitializer.run();

        verify(funcionarioGateway).salvar(funcionarioCaptor.capture());
        Funcionario funcionarioSalvo = funcionarioCaptor.getValue();
        assertThat(funcionarioSalvo.getSenha()).isEqualTo(SENHA_CRIPTOGRAFADA);
        assertThat(funcionarioSalvo.getFuncao()).isEqualTo(Funcao.ADMIN);
        assertThat(funcionarioSalvo.isAtivo()).isTrue();
    }

    @Test
    void naoDeveCriarFuncionarioAdminQuandoJaExistir() {
        when(funcionarioGateway.buscarPorEmail(any())).thenReturn(Optional.of(new Funcionario()));

        adminDataInitializer.run();

        verify(funcionarioGateway, never()).salvar(any());
        verifyNoInteractions(passwordEncoder);
    }
}
