package com.fiap.mecanica.infra.configs.enums;

public enum CodigoErro {

    ENTIDADE_NAO_ENCONTRADA("entidade-nao-encontrada"),
    CONFLITO("conflito"),
    VALIDACAO("validacao"),
    ERRO_INTERNO("erro-interno");

    private final String codigo;

    CodigoErro(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}