package com.fiap.mecanica.atendimento.exception;

public class TemplateJaCadastradoException extends ConflitoException {

    public TemplateJaCadastradoException(String codigo) {
        super("Já existe um template de notificação com o código: " + codigo);
    }
}
