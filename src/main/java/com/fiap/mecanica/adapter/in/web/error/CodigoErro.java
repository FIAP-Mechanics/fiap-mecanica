package com.fiap.mecanica.adapter.in.web.error;

public enum CodigoErro {
    ENTIDADE_NAO_ENCONTRADA("entidade-nao-encontrada"),
    CONFLITO("conflito"),
    VALIDACAO("validacao"),
    NAO_AUTORIZADO("nao-autorizado"),
    ACESSO_NEGADO("acesso-negado"),
    ERRO_INTERNO("erro-interno");

    private final String codigo;

    CodigoErro(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
