package com.fiap.mecanica.estoque.exception;

public class EstoqueInsuficienteException extends ValidacaoException {
    public EstoqueInsuficienteException(String insumo, Long quantidadeDisponivel, Long quantidadeSolicitada) {
        super(String.format("Estoque insuficiente para o insumo '%s'. Disponível: %d, Solicitado: %d",
                insumo, quantidadeDisponivel, quantidadeSolicitada));
    }
}
