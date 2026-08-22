package com.fiap.mecanica.servico.exception;

public class ServicoJaAtivoException extends ValidacaoException {
    public ServicoJaAtivoException(Long id) {
        super("O Serviço com ID " + id + " já se encontra ativo.");
    }
}
