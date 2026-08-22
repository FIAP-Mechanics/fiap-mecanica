package com.fiap.mecanica.application.command;

public record AtualizarVeiculoCommand(String marca, String modelo, String placa, Integer ano) {
}
