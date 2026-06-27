package com.fiap.mecanica.exception;

public class EstoqueInsuficienteException extends BaseException {

    public EstoqueInsuficienteException(String insumo, Long quantidadeDisponivel, Integer quantidadeSolicitada) {
        super(String.format("Estoque insuficiente para o insumo '%s'. Disponível: %d, Solicitado: %d",
                insumo, quantidadeDisponivel, quantidadeSolicitada));
    }
}
