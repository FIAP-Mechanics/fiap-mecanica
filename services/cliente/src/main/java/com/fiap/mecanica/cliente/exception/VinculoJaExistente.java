package com.fiap.mecanica.cliente.exception;

public class VinculoJaExistente extends ConflitoException {
    public VinculoJaExistente(Long clienteId, Long veiculoId) {
        super("Veículo " + veiculoId + " já está vinculado ao cliente " + clienteId);
    }
}
