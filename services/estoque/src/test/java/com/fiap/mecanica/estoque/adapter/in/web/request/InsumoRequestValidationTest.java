package com.fiap.mecanica.estoque.adapter.in.web.request;

import com.fiap.mecanica.estoque.adapter.in.web.controller.EstoqueController;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InsumoRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void deveValidarDadosDoCadastroContraPrecisaoDoBanco() {
        CadastrarInsumoRequest valido = new CadastrarInsumoRequest("Oleo", new BigDecimal("99999999.99"));
        CadastrarInsumoRequest escalaInvalida = new CadastrarInsumoRequest("Oleo", new BigDecimal("1.234"));
        CadastrarInsumoRequest inteiroInvalido = new CadastrarInsumoRequest("Oleo", new BigDecimal("999999999.99"));

        assertThat(validator.validate(valido)).isEmpty();
        assertThat(validator.validate(escalaInvalida)).isNotEmpty();
        assertThat(validator.validate(inteiroInvalido)).isNotEmpty();
    }

    @Test
    void deveValidarSomenteCamposInformadosNaAtualizacao() {
        AtualizarInsumoRequest vazio = new AtualizarInsumoRequest(null, null);
        AtualizarInsumoRequest nomeEmBranco = new AtualizarInsumoRequest("   ", null);
        AtualizarInsumoRequest precoNegativo = new AtualizarInsumoRequest(null, new BigDecimal("-1.00"));
        AtualizarInsumoRequest precoComEscalaInvalida = new AtualizarInsumoRequest(null, new BigDecimal("1.234"));

        assertThat(validator.validate(vazio)).isEmpty();
        assertThat(validator.validate(nomeEmBranco)).isNotEmpty();
        assertThat(validator.validate(precoNegativo)).isNotEmpty();
        assertThat(validator.validate(precoComEscalaInvalida)).isNotEmpty();
    }

    @Test
    void deveAplicarValidacaoNoEndpointDeAtualizacao() throws NoSuchMethodException {
        var requestParameter = EstoqueController.class
                .getMethod("updateInsumo", Long.class, AtualizarInsumoRequest.class)
                .getParameters()[1];

        assertThat(requestParameter.isAnnotationPresent(Valid.class)).isTrue();
    }
}
