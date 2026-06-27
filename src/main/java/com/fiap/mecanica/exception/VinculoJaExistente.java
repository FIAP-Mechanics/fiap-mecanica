package com.fiap.mecanica.exception;

public class VinculoJaExistente extends RuntimeException {
    public VinculoJaExistente(Long clienteId, Long veiculoId) {
        super("Veículo " + veiculoId + " já está vinculado ao cliente " + clienteId);
    }
}
