package com.fiap.mecanica.cliente.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfOuCnpjValidatorTest {

    private CpfOuCnpjValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CpfOuCnpjValidator();
    }

    // CPF válido sem formatação
    @Test
    void deveAceitarCpfValidoSemFormatacao() {
        assertThat(validator.isValid("52998224725", null)).isTrue();
    }

    // CPF válido com formatação
    @Test
    void deveAceitarCpfValidoComFormatacao() {
        assertThat(validator.isValid("529.982.247-25", null)).isTrue();
    }

    // Segundo CPF válido
    @Test
    void deveAceitarOutroCpfValido() {
        assertThat(validator.isValid("111.444.777-35", null)).isTrue();
    }

    // CNPJ válido sem formatação
    @Test
    void deveAceitarCnpjValidoSemFormatacao() {
        assertThat(validator.isValid("11222333000181", null)).isTrue();
    }

    // CNPJ válido com formatação
    @Test
    void deveAceitarCnpjValidoComFormatacao() {
        assertThat(validator.isValid("11.222.333/0001-81", null)).isTrue();
    }

    // CPF com dígitos verificadores errados
    @Test
    void deveRejeitarCpfComDigitoVerificadorErrado() {
        assertThat(validator.isValid("529.982.247-26", null)).isFalse();
    }

    // CPF com todos dígitos iguais (sequência inválida)
    @Test
    void deveRejeitarCpfComTodosDigitosIguais() {
        assertThat(validator.isValid("111.111.111-11", null)).isFalse();
        assertThat(validator.isValid("00000000000", null)).isFalse();
    }

    // CNPJ com dígitos verificadores errados
    @Test
    void deveRejeitarCnpjComDigitoVerificadorErrado() {
        assertThat(validator.isValid("11.222.333/0001-82", null)).isFalse();
    }

    // CNPJ com todos dígitos iguais
    @Test
    void deveRejeitarCnpjComTodosDigitosIguais() {
        assertThat(validator.isValid("11111111111111", null)).isFalse();
    }

    // Comprimento inválido
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
