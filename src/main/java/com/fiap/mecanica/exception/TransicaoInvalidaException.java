package com.fiap.mecanica.exception;

import com.fiap.mecanica.domain.Status;

public class TransicaoInvalidaException extends ConflitoException {
    public TransicaoInvalidaException(Status status, Status novo) {
        super("O atendimento não pode ser transicionada do status \"%s\"para o status \"%s\"".formatted(status.getNome(), novo.getNome()));
    }
}
