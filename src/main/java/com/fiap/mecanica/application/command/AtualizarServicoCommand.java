package com.fiap.mecanica.application.command;

import java.math.BigDecimal;

public record AtualizarServicoCommand(String nome, String descricao, BigDecimal valor) {
}
