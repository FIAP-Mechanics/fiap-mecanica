package com.fiap.mecanica.application.command;

import java.math.BigDecimal;

public record AtualizarInsumoCommand(String nome, BigDecimal precoUnitario) {
}
