package com.fiap.mecanica.adapter.in.web.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfOuCnpjValidatorTest {

    private CpfOuCnpjValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CpfOuCnpjValidator();
    }

    @Test
    void deveAceitarCpfValidoSemFormatacao() {
        assertThat(validator.isValid("52998224725", null)).isTrue();
    }

    @Test
    void deveAceitarCpfValidoComFormatacao() {
        assertThat(validator.isValid("529.982.247-25", null)).isTrue();
    }

    @Test
    void deveAceitarOutroCpfValido() {
        assertThat(validator.isValid("111.444.777-35", null)).isTrue();
    }

    @Test
    void deveAceitarCnpjValidoSemFormatacao() {
        assertThat(validator.isValid("11222333000181", null)).isTrue();
    }

    @Test
    void deveAceitarCnpjValidoComFormatacao() {
        assertThat(validator.isValid("11.222.333/0001-81", null)).isTrue();
    }

    @Test
    void deveRejeitarCpfComDigitoVerificadorErrado() {
        assertThat(validator.isValid("529.982.247-26", null)).isFalse();
    }

    @Test
    void deveRejeitarCpfComTodosDigitosIguais() {
        assertThat(validator.isValid("111.111.111-11", null)).isFalse();
        assertThat(validator.isValid("00000000000", null)).isFalse();
    }

    @Test
    void deveRejeitarCnpjComDigitoVerificadorErrado() {
        assertThat(validator.isValid("11.222.333/0001-82", null)).isFalse();
    }

    @Test
    void deveRejeitarCnpjComTodosDigitosIguais() {
        assertThat(validator.isValid("11111111111111", null)).isFalse();
    }

    @Test
    void deveRejeitarDocumentoComTamanhoInvalido() {
        assertThat(validator.isValid("123456789", null)).isFalse();
        assertThat(validator.isValid("1234567890123456", null)).isFalse();
    }

    @Test
    void deveRejeitarNull() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @Test
    void deveRejeitarStringVazia() {
        assertThat(validator.isValid("", null)).isFalse();
        assertThat(validator.isValid("   ", null)).isFalse();
    }
}
