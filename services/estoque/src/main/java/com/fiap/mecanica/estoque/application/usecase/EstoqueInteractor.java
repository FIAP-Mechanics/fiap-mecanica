package com.fiap.mecanica.estoque.application.usecase;

import com.fiap.mecanica.estoque.application.command.AtualizarInsumoCommand;
import com.fiap.mecanica.estoque.application.command.DeduzirEstoqueItemCommand;
import com.fiap.mecanica.estoque.application.port.in.EstoqueUseCase;
import com.fiap.mecanica.estoque.application.port.out.EstoqueGateway;
import com.fiap.mecanica.estoque.domain.Estoque;
import com.fiap.mecanica.estoque.domain.Insumo;
import com.fiap.mecanica.estoque.exception.EstoqueInativoException;
import com.fiap.mecanica.estoque.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.estoque.exception.EstoqueJaAtivoException;
import com.fiap.mecanica.estoque.exception.EstoqueNotFound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EstoqueInteractor implements EstoqueUseCase {

    private final EstoqueGateway estoqueGateway;

    public EstoqueInteractor(EstoqueGateway estoqueGateway) {
        this.estoqueGateway = estoqueGateway;
    }

    @Override
    public Estoque cadastrarEstoque(Estoque estoque) {
        return estoqueGateway.salvar(estoque);
    }

    @Override
    public Estoque buscarPorIdInsumo(Long idInsumo) {
        Estoque estoque = buscarRegistro(idInsumo);
        if (!estoque.isAtivo()) throw new EstoqueInativoException(idInsumo);
        return estoque;
    }

    @Override
    public List<Estoque> listarEstoque() {
        return estoqueGateway.buscarTodosAtivos();
    }

    @Override
    public Estoque atualizarQuantidade(Long idInsumo, Long quantidade) {
        Estoque estoque = buscarPorIdInsumo(idInsumo);
        estoque.setQuantidadeInsumo(quantidade);
        return estoqueGateway.salvar(estoque);
    }

    @Override
    public Estoque atualizarInsumo(Long idInsumo, AtualizarInsumoCommand command) {
        Estoque estoque = buscarPorIdInsumo(idInsumo);
        Insumo insumo = estoque.getInsumo();
        atualizaSeExistente(command.nome(), insumo::setNome);
        atualizaSeExistente(command.precoUnitario(), insumo::setPrecoUnitario);
        return estoqueGateway.salvar(estoque);
    }

    @Override
    public Estoque excluirEstoque(Long idInsumo) {
        Estoque estoque = buscarPorIdInsumo(idInsumo);
        estoque.setAtivo(false);
        return estoqueGateway.salvar(estoque);
    }

    @Override
    public void deduzirEstoque(List<DeduzirEstoqueItemCommand> itens) {
        List<Estoque> estoquesAtualizados = new ArrayList<>();
        for (DeduzirEstoqueItemCommand item : itens) {
            Estoque estoque = buscarPorIdInsumo(item.insumoId());
            if (estoque.getQuantidadeInsumo() < item.quantidade()) {
                throw new EstoqueInsuficienteException(
                        estoque.getInsumo().getNome(),
                        estoque.getQuantidadeInsumo(),
                        item.quantidade()
                );
            }
            estoque.setQuantidadeInsumo(estoque.getQuantidadeInsumo() - item.quantidade());
            estoquesAtualizados.add(estoque);
        }
        estoqueGateway.salvarTodos(estoquesAtualizados);
    }

    @Override
    public Estoque reativarEstoque(Long idInsumo) {
        Estoque estoque = buscarRegistro(idInsumo);
        if (estoque.isAtivo()) throw new EstoqueJaAtivoException(idInsumo);
        estoque.setAtivo(true);
        return estoqueGateway.salvar(estoque);
    }

    private Estoque buscarRegistro(Long idInsumo) {
        return estoqueGateway.buscarPorIdInsumo(idInsumo).orElseThrow(() -> new EstoqueNotFound(idInsumo));
    }

    private <T> void atualizaSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) {
            setter.accept(valor);
        }
    }
}
