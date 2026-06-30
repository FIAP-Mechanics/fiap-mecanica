package com.fiap.mecanica.config;

import com.fiap.mecanica.domain.Funcao;
import com.fiap.mecanica.domain.Funcionario;
import com.fiap.mecanica.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDataInitializerTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminDataInitializer adminDataInitializer;

    private final String adminEmail = "admin@test.com";
    private final String adminPassword = "password123";
    private final String adminNome = "Admin Test";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminDataInitializer, "adminEmail", adminEmail);
        ReflectionTestUtils.setField(adminDataInitializer, "adminPassword", adminPassword);
        ReflectionTestUtils.setField(adminDataInitializer, "adminNome", adminNome);
    }

    @Test
    void deveCriarAdminPadraoQuandoNaoExistir() {
        // Arrange
        when(funcionarioRepository.findByEmail(adminEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(adminPassword)).thenReturn("encodedPassword");

        // Act
        adminDataInitializer.run();

        // Assert
        ArgumentCaptor<Funcionario> captor = ArgumentCaptor.forClass(Funcionario.class);
        verify(funcionarioRepository).save(captor.capture());
        
        Funcionario adminCreated = captor.getValue();
        assertThat(adminCreated.getEmail()).isEqualTo(adminEmail);
        assertThat(adminCreated.getSenha()).isEqualTo("encodedPassword");
        assertThat(adminCreated.getFuncao()).isEqualTo(Funcao.ADMIN);
        assertThat(adminCreated.getNome()).isEqualTo(adminNome);
        assertThat(adminCreated.isAtivo()).isTrue();
    }

    @Test
    void naoDeveCriarAdminPadraoQuandoJaExistir() {
        // Arrange
        when(funcionarioRepository.findByEmail(adminEmail)).thenReturn(Optional.of(new Funcionario()));

        // Act
        adminDataInitializer.run();

        // Assert
        verify(funcionarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
