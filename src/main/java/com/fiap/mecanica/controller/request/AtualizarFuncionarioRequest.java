package com.fiap.mecanica.controller.request;

import com.fiap.mecanica.domain.Funcao;

public record AtualizarFuncionarioRequest(String email, String nome, String senha, Funcao funcao) {
}
