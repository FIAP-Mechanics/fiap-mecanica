package com.fiap.mecanica.domain.exception;

import com.fiap.mecanica.domain.Status;
import com.fiap.mecanica.exception.ConflitoException;

public class TransicaoInvalidaException extends ConflitoException {

    public TransicaoInvalidaException(Status status, Status novoStatus) {
        super("O atendimento não pode ser transicionada do status \"%s\"para o status \"%s\""
                .formatted(status.getNome(), novoStatus.getNome()));
    }
}
