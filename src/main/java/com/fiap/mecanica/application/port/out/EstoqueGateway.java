package com.fiap.mecanica.application.port.out;

import com.fiap.mecanica.domain.Estoque;

import java.util.List;
import java.util.Optional;

public interface EstoqueGateway {
    List<Estoque> buscarAtivos();
    Optional<Estoque> buscarPorInsumoId(Long idInsumo);
    Estoque salvar(Estoque estoque);
}
