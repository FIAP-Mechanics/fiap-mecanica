package com.fiap.mecanica.funcionario.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.funcionario.adapter.out.persistence.jpa.entity.FuncionarioJpaEntity;
import com.fiap.mecanica.funcionario.domain.Funcao;
import com.fiap.mecanica.funcionario.domain.Funcionario;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FuncionarioJpaMapperTest {

    private static final Long ID = 1L;
    private static final String EMAIL = "joao@mecanica.com";
    private static final String SENHA = "senha-codificada";
    private static final String NOME = "João Silva";
    private static final Funcao FUNCAO = Funcao.MECANICO;

    // ===================== toDomain =====================

    @Test
    void deveConverterEntityParaDomainComTodosOsCampos() {
        FuncionarioJpaEntity entity = criarEntity();

        Funcionario funcionario = FuncionarioJpaMapper.toDomain(entity);

        assertThat(funcionario.getId()).isEqualTo(ID);
        assertThat(funcionario.getEmail()).isEqualTo(EMAIL);
        assertThat(funcionario.getSenha()).isEqualTo(SENHA);
        assertThat(funcionario.getNome()).isEqualTo(NOME);
        assertThat(funcionario.getFuncao()).isEqualTo(FUNCAO);
        assertThat(funcionario.isAtivo()).isTrue();
    }

    @Test
    void deveConverterEntityInativaParaDomainInativo() {
        FuncionarioJpaEntity entity = criarEntity();
        entity.setAtivo(false);

        Funcionario funcionario = FuncionarioJpaMapper.toDomain(entity);

        assertThat(funcionario.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarNuloAoConverterEntityNulaParaDomain() {
        assertThat(FuncionarioJpaMapper.toDomain(null)).isNull();
    }

    // ===================== toJpaEntity =====================

    @Test
    void deveConverterDomainParaEntityComTodosOsCampos() {
        Funcionario funcionario = criarFuncionario();

        FuncionarioJpaEntity entity = FuncionarioJpaMapper.toJpaEntity(funcionario);

        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getEmail()).isEqualTo(EMAIL);
        assertThat(entity.getSenha()).isEqualTo(SENHA);
        assertThat(entity.getNome()).isEqualTo(NOME);
        assertThat(entity.getFuncao()).isEqualTo(FUNCAO);
        assertThat(entity.isAtivo()).isTrue();
    }

    @Test
    void deveConverterDomainInativoParaEntityInativa() {
        Funcionario funcionario = criarFuncionario();
        funcionario.setAtivo(false);

        FuncionarioJpaEntity entity = FuncionarioJpaMapper.toJpaEntity(funcionario);

        assertThat(entity.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarNuloAoConverterDomainNuloParaEntity() {
        assertThat(FuncionarioJpaMapper.toJpaEntity(null)).isNull();
    }

    private FuncionarioJpaEntity criarEntity() {
        return FuncionarioJpaEntity.builder()
                .id(ID)
                .email(EMAIL)
                .senha(SENHA)
                .nome(NOME)
                .funcao(FUNCAO)
                .ativo(true)
                .build();
    }

    private Funcionario criarFuncionario() {
        return Funcionario.builder()
                .id(ID)
                .email(EMAIL)
                .senha(SENHA)
                .nome(NOME)
                .funcao(FUNCAO)
                .ativo(true)
                .build();
    }
}
