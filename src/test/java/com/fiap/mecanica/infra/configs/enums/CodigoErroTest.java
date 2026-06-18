package com.fiap.mecanica.infra.configs.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodigoErroTest {

    @Test
    void deveRetornarCodigoParaEntidadeNaoEncontrada() {
        assertThat(CodigoErro.ENTIDADE_NAO_ENCONTRADA.getCodigo())
                .isEqualTo("entidade-nao-encontrada");
    }

    @Test
    void deveRetornarCodigoParaConflito() {
        assertThat(CodigoErro.CONFLITO.getCodigo())
                .isEqualTo("conflito");
    }

    @Test
    void deveRetornarCodigoParaValidacao() {
        assertThat(CodigoErro.VALIDACAO.getCodigo())
                .isEqualTo("validacao");
    }

    @Test
    void deveRetornarCodigoParaErroInterno() {
        assertThat(CodigoErro.ERRO_INTERNO.getCodigo())
                .isEqualTo("erro-interno");
    }

    @Test
    void deveConterTodosOsCodigos() {
        assertThat(CodigoErro.values()).hasSize(4);
    }

    @Test
    void deveObterEnumPorNome() {
        assertThat(CodigoErro.valueOf("ENTIDADE_NAO_ENCONTRADA"))
                .isEqualTo(CodigoErro.ENTIDADE_NAO_ENCONTRADA);
    }
}