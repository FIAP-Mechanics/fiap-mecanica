package com.fiap.mecanica.funcionario.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.funcionario.adapter.out.persistence.jpa.entity.FuncionarioJpaEntity;
import com.fiap.mecanica.funcionario.adapter.out.persistence.jpa.repository.FuncionarioSpringDataRepository;
import com.fiap.mecanica.funcionario.domain.Funcao;
import com.fiap.mecanica.funcionario.domain.Funcionario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuncionarioJpaGatewayTest {

    private static final Long ID = 1L;
    private static final String EMAIL = "joao@mecanica.com";
    private static final String NOME = "João Silva";

    @Mock
    private FuncionarioSpringDataRepository repository;

    @InjectMocks
    private FuncionarioJpaGateway gateway;

    // ===================== buscarTodos =====================

    @Test
    void deveRetornarTodosOsFuncionariosConvertidosParaDomain() {
        when(repository.findAll()).thenReturn(List.of(criarEntity()));

        List<Funcionario> resultado = gateway.buscarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getId()).isEqualTo(ID);
        verify(repository).findAll();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverFuncionarios() {
        when(repository.findAll()).thenReturn(List.of());

        List<Funcionario> resultado = gateway.buscarTodos();

        assertThat(resultado).isEmpty();
    }

    // ===================== buscarPorId =====================

    @Test
    void deveRetornarFuncionarioQuandoIdExistir() {
        when(repository.findById(ID)).thenReturn(Optional.of(criarEntity()));

        Optional<Funcionario> resultado = gateway.buscarPorId(ID);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(ID);
    }

    @Test
    void deveRetornarOptionalVazioQuandoIdNaoExistir() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        Optional<Funcionario> resultado = gateway.buscarPorId(ID);

        assertThat(resultado).isEmpty();
    }

    // ===================== buscarPorEmail =====================

    @Test
    void deveRetornarFuncionarioQuandoEmailExistir() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(criarEntity()));

        Optional<Funcionario> resultado = gateway.buscarPorEmail(EMAIL);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    void deveRetornarOptionalVazioQuandoEmailNaoExistir() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        Optional<Funcionario> resultado = gateway.buscarPorEmail(EMAIL);

        assertThat(resultado).isEmpty();
    }

    // ===================== existePorEmail =====================

    @Test
    void deveRetornarTrueQuandoEmailJaExistir() {
        when(repository.existsByEmail(EMAIL)).thenReturn(true);

        assertThat(gateway.existePorEmail(EMAIL)).isTrue();
    }

    @Test
    void deveRetornarFalseQuandoEmailNaoExistir() {
        when(repository.existsByEmail(EMAIL)).thenReturn(false);

        assertThat(gateway.existePorEmail(EMAIL)).isFalse();
    }

    // ===================== salvar =====================

    @Test
    void deveSalvarFuncionarioERetornarDomainConvertido() {
        Funcionario funcionario = criarFuncionario();
        when(repository.save(any())).thenReturn(criarEntity());

        Funcionario resultado = gateway.salvar(funcionario);

        assertThat(resultado.getId()).isEqualTo(ID);
        assertThat(resultado.getEmail()).isEqualTo(EMAIL);
    }

    private FuncionarioJpaEntity criarEntity() {
        return FuncionarioJpaEntity.builder()
                .id(ID)
                .email(EMAIL)
                .nome(NOME)
                .funcao(Funcao.MECANICO)
                .ativo(true)
                .build();
    }

    private Funcionario criarFuncionario() {
        return Funcionario.builder()
                .id(ID)
                .email(EMAIL)
                .nome(NOME)
                .funcao(Funcao.MECANICO)
                .ativo(true)
                .build();
    }
}
