package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity.FuncionarioJpaEntity;
import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.repository.FuncionarioSpringDataRepository;
import com.fiap.mecanica.atendimento.domain.Funcao;
import com.fiap.mecanica.atendimento.domain.Funcionario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuncionarioJpaGatewayTest {

    private static final Long ID = 1L;
    private static final String EMAIL = "funcionario@fiap.com";
    private static final String SENHA = "hash";
    private static final String NOME = "Joao";

    @Mock
    private FuncionarioSpringDataRepository repository;

    @InjectMocks
    private FuncionarioJpaGateway gateway;

    // ===================== buscarPorEmail =====================

    @Test
    void deveRetornarFuncionarioQuandoEmailExistir() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(criarEntity()));

        Optional<Funcionario> resultado = gateway.buscarPorEmail(EMAIL);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(ID);
        assertThat(resultado.get().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    void deveRetornarOptionalVazioQuandoEmailNaoExistir() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        Optional<Funcionario> resultado = gateway.buscarPorEmail(EMAIL);

        assertThat(resultado).isEmpty();
    }

    // ===================== salvar =====================

    @Test
    void deveSalvarFuncionarioERetornarDomainConvertido() {
        Funcionario funcionario = criarFuncionario();
        when(repository.save(any())).thenReturn(criarEntity());

        Funcionario resultado = gateway.salvar(funcionario);

        assertThat(resultado.getId()).isEqualTo(ID);
        assertThat(resultado.getEmail()).isEqualTo(EMAIL);
        assertThat(resultado.getNome()).isEqualTo(NOME);
    }

    private FuncionarioJpaEntity criarEntity() {
        return FuncionarioJpaEntity.builder()
                .id(ID)
                .email(EMAIL)
                .senha(SENHA)
                .nome(NOME)
                .funcao(Funcao.ADMIN)
                .ativo(true)
                .build();
    }

    private Funcionario criarFuncionario() {
        return Funcionario.builder()
                .id(ID)
                .email(EMAIL)
                .senha(SENHA)
                .nome(NOME)
                .funcao(Funcao.ADMIN)
                .ativo(true)
                .build();
    }
}
