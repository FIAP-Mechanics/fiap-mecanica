package com.fiap.mecanica.atendimento.config;

import com.fiap.mecanica.atendimento.domain.Funcao;
import com.fiap.mecanica.atendimento.domain.Funcionario;
import com.fiap.mecanica.atendimento.repository.FuncionarioRepository;
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
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminDataInitializer adminDataInitializer;

    @Captor
    private ArgumentCaptor<Funcionario> funcionarioCaptor;

    // ===================== run =====================

    @Test
    void deveCriarFuncionarioAdminQuandoNaoExistir() {
        when(funcionarioRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn(SENHA_CRIPTOGRAFADA);

        adminDataInitializer.run();

        verify(funcionarioRepository).save(funcionarioCaptor.capture());
        Funcionario funcionarioSalvo = funcionarioCaptor.getValue();
        assertThat(funcionarioSalvo.getSenha()).isEqualTo(SENHA_CRIPTOGRAFADA);
        assertThat(funcionarioSalvo.getFuncao()).isEqualTo(Funcao.ADMIN);
        assertThat(funcionarioSalvo.isAtivo()).isTrue();
    }

    @Test
    void naoDeveCriarFuncionarioAdminQuandoJaExistir() {
        when(funcionarioRepository.findByEmail(any())).thenReturn(Optional.of(new Funcionario()));

        adminDataInitializer.run();

        verify(funcionarioRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }
}
