package com.fiap.mecanica.infra.configs.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RespostaErroTest {

    @Test
    void deveCriarRespostaErroComLista() {
        ErroDetalhe erro1 = new ErroDetalhe("validacao", "Campo obrigatório");
        ErroDetalhe erro2 = new ErroDetalhe("conflito", "Recurso duplicado");
        List<ErroDetalhe> erros = List.of(erro1, erro2);

        RespostaErro respostaErro = new RespostaErro(erros);

        assertThat(respostaErro.erros()).hasSize(2);
        assertThat(respostaErro.erros()).containsExactly(erro1, erro2);
    }

    @Test
    void deveCriarRespostaErroComUmErro() {
        ErroDetalhe erro = new ErroDetalhe("validacao", "Valor inválido");
        List<ErroDetalhe> erros = List.of(erro);

        RespostaErro respostaErro = new RespostaErro(erros);

        assertThat(respostaErro.erros()).hasSize(1);
        assertThat(respostaErro.erros().get(0)).isEqualTo(erro);
    }

    @Test
    void deveCriarRespostaErroComListaVazia() {
        List<ErroDetalhe> erros = List.of();

        RespostaErro respostaErro = new RespostaErro(erros);

        assertThat(respostaErro.erros()).isEmpty();
    }

    @Test
    void deveEqualsParaRespostasErroIguais() {
        ErroDetalhe erro = new ErroDetalhe("validacao", "Valor inválido");
        List<ErroDetalhe> erros = List.of(erro);

        RespostaErro resposta1 = new RespostaErro(erros);
        RespostaErro resposta2 = new RespostaErro(erros);

        assertThat(resposta1).isEqualTo(resposta2);
    }

    @Test
    void deveHashCodeIgualParaRespostasErroIguais() {
        ErroDetalhe erro = new ErroDetalhe("validacao", "Valor inválido");
        List<ErroDetalhe> erros = List.of(erro);

        RespostaErro resposta1 = new RespostaErro(erros);
        RespostaErro resposta2 = new RespostaErro(erros);

        assertThat(resposta1).hasSameHashCodeAs(resposta2);
    }

    @Test
    void deveToStringConterErros() {
        ErroDetalhe erro = new ErroDetalhe("validacao", "Valor inválido");
        List<ErroDetalhe> erros = List.of(erro);

        RespostaErro respostaErro = new RespostaErro(erros);

        assertThat(respostaErro.toString()).isNotNull();
    }
}