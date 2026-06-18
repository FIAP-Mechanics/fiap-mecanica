package com.fiap.mecanica.infra.configs.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErroDetalheTest {

    @Test
    void deveCriarErroDetalheComCodigo() {
        String codigo = "erro-validacao";
        String descricao = "Campo obrigatório";

        ErroDetalhe erroDetalhe = new ErroDetalhe(codigo, descricao);

        assertThat(erroDetalhe.codigo()).isEqualTo(codigo);
        assertThat(erroDetalhe.descricao()).isEqualTo(descricao);
    }

    @Test
    void deveAcessarCamposDoErroDetalhe() {
        ErroDetalhe erroDetalhe = new ErroDetalhe("validacao", "Valor inválido");

        assertThat(erroDetalhe.codigo()).isNotNull();
        assertThat(erroDetalhe.descricao()).isNotNull();
    }

    @Test
    void deveEqualsParaErroDetalhesIguais() {
        ErroDetalhe erro1 = new ErroDetalhe("validacao", "Valor inválido");
        ErroDetalhe erro2 = new ErroDetalhe("validacao", "Valor inválido");

        assertThat(erro1).isEqualTo(erro2);
    }

    @Test
    void deveHashCodeIgualParaErroDetalhesIguais() {
        ErroDetalhe erro1 = new ErroDetalhe("validacao", "Valor inválido");
        ErroDetalhe erro2 = new ErroDetalhe("validacao", "Valor inválido");

        assertThat(erro1).hasSameHashCodeAs(erro2);
    }

    @Test
    void deveToStringConterCodigo() {
        ErroDetalhe erroDetalhe = new ErroDetalhe("validacao", "Valor inválido");

        assertThat(erroDetalhe.toString()).contains("validacao");
    }
}