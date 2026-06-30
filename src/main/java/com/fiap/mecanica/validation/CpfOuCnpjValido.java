package com.fiap.mecanica.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CpfOuCnpjValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfOuCnpjValido {
    String message() default "Documento deve ser um CPF ou CNPJ válido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
