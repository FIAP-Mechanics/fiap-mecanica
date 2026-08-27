package com.fiap.mecanica.estoque.application.port.out;

import com.fiap.mecanica.estoque.domain.Estoque;

import java.util.List;
import java.util.Optional;

public interface EstoqueGateway {

    List<Estoque> buscarTodosAtivos();

    Optional<Estoque> buscarPorIdInsumo(Long idInsumo);

    Estoque salvar(Estoque estoque);

    List<Estoque> salvarTodos(List<Estoque> estoques);
}
