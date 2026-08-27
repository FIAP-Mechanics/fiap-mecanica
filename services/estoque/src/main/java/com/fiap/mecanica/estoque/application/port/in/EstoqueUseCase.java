package com.fiap.mecanica.estoque.application.port.in;

import com.fiap.mecanica.estoque.application.command.AtualizarInsumoCommand;
import com.fiap.mecanica.estoque.application.command.DeduzirEstoqueItemCommand;
import com.fiap.mecanica.estoque.domain.Estoque;

import java.util.List;

public interface EstoqueUseCase {

    List<Estoque> listarEstoque();

    Estoque buscarPorIdInsumo(Long idInsumo);

    Estoque cadastrarEstoque(Estoque estoque);

    Estoque atualizarQuantidade(Long idInsumo, Long quantidade);

    Estoque atualizarInsumo(Long idInsumo, AtualizarInsumoCommand command);

    Estoque excluirEstoque(Long idInsumo);

    Estoque reativarEstoque(Long idInsumo);

    void deduzirEstoque(List<DeduzirEstoqueItemCommand> itens);
}
