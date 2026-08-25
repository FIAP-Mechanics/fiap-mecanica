package com.fiap.mecanica.atendimento.adapter.out.security;

import com.fiap.mecanica.atendimento.application.port.out.FuncionarioGateway;
import com.fiap.mecanica.atendimento.domain.Funcao;
import com.fiap.mecanica.atendimento.domain.Funcionario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuncionarioUserDetailsServiceTest {

    @Mock
    private FuncionarioGateway funcionarioGateway;

    @InjectMocks
    private FuncionarioUserDetailsService service;

    @Test
    void deveCarregarFuncionarioAtivoComRoleDaFuncao() {
        when(funcionarioGateway.buscarPorEmail("mecanico@mecanica.com"))
                .thenReturn(Optional.of(criarFuncionario(true, Funcao.MECANICO)));

        var userDetails = service.loadUserByUsername("mecanico@mecanica.com");

        assertThat(userDetails.getUsername()).isEqualTo("mecanico@mecanica.com");
        assertThat(userDetails.getPassword()).isEqualTo("senha-codificada");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_MECANICO");
    }

    @Test
    void deveCarregarFuncionarioInativoComoDesabilitado() {
        when(funcionarioGateway.buscarPorEmail("atendente@mecanica.com"))
                .thenReturn(Optional.of(criarFuncionario(false, Funcao.ATENDENTE)));

        var userDetails = service.loadUserByUsername("atendente@mecanica.com");

        assertThat(userDetails.isEnabled()).isFalse();
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ATENDENTE");
    }

    @Test
    void deveRejeitarFuncionarioInexistente() {
        when(funcionarioGateway.buscarPorEmail("ninguem@mecanica.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ninguem@mecanica.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private Funcionario criarFuncionario(boolean ativo, Funcao funcao) {
        return Funcionario.builder()
                .email(funcao.name().toLowerCase() + "@mecanica.com")
                .senha("senha-codificada")
                .nome("Funcionario")
                .funcao(funcao)
                .ativo(ativo)
                .build();
    }
}
