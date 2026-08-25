package com.fiap.mecanica.estoque.adapter.in.web.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CodigoErro {
    ENTIDADE_NAO_ENCONTRADA("entidade-nao-encontrada"),
    CONFLITO("conflito"),
    VALIDACAO("validacao"),
    NAO_AUTORIZADO("nao-autorizado"),
    ACESSO_NEGADO("acesso-negado"),
    ERRO_INTERNO("erro-interno");

    private final String codigo;
}
