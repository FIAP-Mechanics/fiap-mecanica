package com.fiap.mecanica.adapter.in.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = CpfOuCnpjValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfOuCnpjValido {
    String message() default "Documento deve ser um CPF ou CNPJ válido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
