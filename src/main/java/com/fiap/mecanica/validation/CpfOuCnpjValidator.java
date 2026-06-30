package com.fiap.mecanica.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfOuCnpjValidator implements ConstraintValidator<CpfOuCnpjValido, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String digits = value.replaceAll("[.\\-/]", "");
        return switch (digits.length()) {
            case 11 -> isValidCpf(digits);
            case 14 -> isValidCnpj(digits);
            default -> false;
        };
    }

    private boolean isValidCpf(String cpf) {
        if (!cpf.chars().allMatch(Character::isDigit)) return false;
        if (cpf.chars().distinct().count() == 1) return false;

        int[] weights1 = {10, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

        return calcularDigito(cpf, weights1, 9) == Character.getNumericValue(cpf.charAt(9))
                && calcularDigito(cpf, weights2, 10) == Character.getNumericValue(cpf.charAt(10));
    }

    private boolean isValidCnpj(String cnpj) {
        if (!cnpj.chars().allMatch(Character::isDigit)) return false;
        if (cnpj.chars().distinct().count() == 1) return false;

        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        return calcularDigito(cnpj, weights1, 12) == Character.getNumericValue(cnpj.charAt(12))
                && calcularDigito(cnpj, weights2, 13) == Character.getNumericValue(cnpj.charAt(13));
    }

    private int calcularDigito(String value, int[] weights, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Character.getNumericValue(value.charAt(i)) * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
