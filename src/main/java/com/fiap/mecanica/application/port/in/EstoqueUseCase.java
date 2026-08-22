package com.fiap.mecanica.application.port.in;

import com.fiap.mecanica.application.command.AtualizarInsumoCommand;
import com.fiap.mecanica.domain.Estoque;
import com.fiap.mecanica.domain.OrdemServicoInsumo;

import java.util.List;

public interface EstoqueUseCase {
    Estoque cadastrarEstoque(Estoque estoque);
    Estoque buscarPorIdInsumo(Long idInsumo);
    List<Estoque> listarEstoque();
    Estoque atualizarQuantidade(Long idInsumo, Long quantidade);
    Estoque atualizarInsumo(Long idInsumo, AtualizarInsumoCommand command);
    Estoque excluirEstoque(Long idInsumo);
    void deduzirEstoque(List<OrdemServicoInsumo> insumos);
    Estoque reativarEstoque(Long idInsumo);
}
