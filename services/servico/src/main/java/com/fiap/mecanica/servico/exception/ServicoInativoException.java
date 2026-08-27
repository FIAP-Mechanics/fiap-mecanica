package com.fiap.mecanica.servico.exception;

public class ServicoInativoException extends ValidacaoException {
    public ServicoInativoException(Long id) {
        super("O Serviço com ID " + id + " encontra-se inativo");
    }
}
