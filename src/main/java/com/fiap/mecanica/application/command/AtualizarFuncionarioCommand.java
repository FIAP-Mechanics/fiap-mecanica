package com.fiap.mecanica.application.command;

import com.fiap.mecanica.domain.Funcao;

public record AtualizarFuncionarioCommand(String email, String senha, String nome, Funcao funcao) {
}
