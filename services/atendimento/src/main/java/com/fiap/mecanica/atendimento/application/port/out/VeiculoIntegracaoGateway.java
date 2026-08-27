package com.fiap.mecanica.atendimento.application.port.out;

public interface VeiculoIntegracaoGateway {

    /**
     * Valida a existência e o status ativo do veículo, lançando as exceções de domínio
     * correspondentes em caso de falha. O resultado da consulta não é utilizado pela
     * lógica de negócio hoje, por isso a porta não expõe nenhum dado de retorno.
     */
    void buscarVeiculo(Long id);
}
