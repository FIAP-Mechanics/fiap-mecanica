package com.fiap.mecanica.servico.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.servico.adapter.out.persistence.jpa.entity.ServicoJpaEntity;
import com.fiap.mecanica.servico.domain.Servico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ServicoJpaMapperTest {

    private static final Long ID = 1L;
    private static final String NOME = "Alinhamento";
    private static final String DESCRICAO = "Alinhamento das rodas";
    private static final BigDecimal VALOR = new BigDecimal("100.00");

    // ===================== toDomain =====================

    @Test
    void deveConverterEntityParaDomainComSucesso() {
        ServicoJpaEntity entity = criarEntity(true);

        Servico servico = ServicoJpaMapper.toDomain(entity);

        assertThat(servico.getId()).isEqualTo(ID);
        assertThat(servico.getNome()).isEqualTo(NOME);
        assertThat(servico.getDescricao()).isEqualTo(DESCRICAO);
        assertThat(servico.getValor()).isEqualByComparingTo(VALOR);
        assertThat(servico.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarNuloAoConverterEntityNulaParaDomain() {
        assertThat(ServicoJpaMapper.toDomain(null)).isNull();
    }

    // ===================== toJpaEntity =====================

    @Test
    void deveConverterDomainParaEntityComSucesso() {
        Servico servico = criarServico(false);

        ServicoJpaEntity entity = ServicoJpaMapper.toJpaEntity(servico);

        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getNome()).isEqualTo(NOME);
        assertThat(entity.getDescricao()).isEqualTo(DESCRICAO);
        assertThat(entity.getValor()).isEqualByComparingTo(VALOR);
        assertThat(entity.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarNuloAoConverterDomainNuloParaEntity() {
        assertThat(ServicoJpaMapper.toJpaEntity(null)).isNull();
    }

    private ServicoJpaEntity criarEntity(boolean ativo) {
        return ServicoJpaEntity.builder()
                .id(ID)
                .nome(NOME)
                .descricao(DESCRICAO)
                .valor(VALOR)
                .ativo(ativo)
                .build();
    }

    private Servico criarServico(boolean ativo) {
        return Servico.builder()
                .id(ID)
                .nome(NOME)
                .descricao(DESCRICAO)
                .valor(VALOR)
                .ativo(ativo)
                .build();
    }
}
