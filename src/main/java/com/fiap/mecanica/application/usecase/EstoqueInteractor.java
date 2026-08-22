package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AtualizarInsumoCommand;
import com.fiap.mecanica.application.port.in.EstoqueUseCase;
import com.fiap.mecanica.application.port.out.EstoqueGateway;
import com.fiap.mecanica.application.port.out.NotificacaoGateway;
import com.fiap.mecanica.application.port.out.TransacaoGateway;
import com.fiap.mecanica.domain.CodigoTemplate;
import com.fiap.mecanica.domain.Estoque;
import com.fiap.mecanica.domain.Insumo;
import com.fiap.mecanica.domain.OrdemServicoInsumo;
import com.fiap.mecanica.exception.EstoqueInativoException;
import com.fiap.mecanica.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.exception.EstoqueJaAtivoException;
import com.fiap.mecanica.exception.EstoqueNotFound;

import java.util.List;
import java.util.function.Consumer;

public class EstoqueInteractor implements EstoqueUseCase {

    private final EstoqueGateway estoqueGateway;
    private final NotificacaoGateway notificacaoGateway;
    private final TransacaoGateway transacaoGateway;

    public EstoqueInteractor(
            EstoqueGateway estoqueGateway,
            NotificacaoGateway notificacaoGateway,
            TransacaoGateway transacaoGateway) {
        this.estoqueGateway = estoqueGateway;
        this.notificacaoGateway = notificacaoGateway;
        this.transacaoGateway = transacaoGateway;
    }

    @Override
    public Estoque cadastrarEstoque(Estoque estoque) {
        return estoqueGateway.salvar(estoque);
    }

    @Override
    public Estoque buscarPorIdInsumo(Long idInsumo) {
        Estoque estoque = buscarRegistro(idInsumo);
        if (!estoque.isAtivo()) {
            throw new EstoqueInativoException(idInsumo);
        }
        return estoque;
    }

    @Override
    public List<Estoque> listarEstoque() {
        return estoqueGateway.buscarAtivos();
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
        atualizarSeExistente(command.nome(), insumo::setNome);
        atualizarSeExistente(command.precoUnitario(), insumo::setPrecoUnitario);
        return estoqueGateway.salvar(estoque);
    }

    @Override
    public Estoque excluirEstoque(Long idInsumo) {
        Estoque estoque = buscarPorIdInsumo(idInsumo);
        estoque.setAtivo(false);
        return estoqueGateway.salvar(estoque);
    }

    @Override
    public void deduzirEstoque(List<OrdemServicoInsumo> insumos) {
        transacaoGateway.executar(() -> {
            for (OrdemServicoInsumo ordemServicoInsumo : insumos) {
                Estoque estoque = buscarPorIdInsumo(ordemServicoInsumo.getInsumo().getId());
                if (estoque.getQuantidadeInsumo() < ordemServicoInsumo.getQuantidade()) {
                    notificarNecessidadeReposicao(estoque, ordemServicoInsumo);
                    throw new EstoqueInsuficienteException(
                            ordemServicoInsumo.getInsumo().getNome(),
                            estoque.getQuantidadeInsumo(),
                            ordemServicoInsumo.getQuantidade());
                }
                estoque.setQuantidadeInsumo(
                        estoque.getQuantidadeInsumo() - ordemServicoInsumo.getQuantidade());
                estoqueGateway.salvar(estoque);
            }
        });
    }

    @Override
    public Estoque reativarEstoque(Long idInsumo) {
        Estoque estoque = buscarRegistro(idInsumo);
        if (estoque.isAtivo()) {
            throw new EstoqueJaAtivoException(idInsumo);
        }
        estoque.setAtivo(true);
        return estoqueGateway.salvar(estoque);
    }

    private Estoque buscarRegistro(Long idInsumo) {
        return estoqueGateway.buscarPorInsumoId(idInsumo)
                .orElseThrow(() -> new EstoqueNotFound(idInsumo));
    }

    private <T> void atualizarSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) {
            setter.accept(valor);
        }
    }

    private void notificarNecessidadeReposicao(
            Estoque estoque,
            OrdemServicoInsumo ordemServicoInsumo) {
        try {
            String nomeInsumo = ordemServicoInsumo.getInsumo().getNome() != null
                    ? ordemServicoInsumo.getInsumo().getNome()
                    : estoque.getInsumo().getNome();
            notificacaoGateway.notificarFuncionarios(
                    CodigoTemplate.REPOSICAO_ESTOQUE,
                    nomeInsumo,
                    String.valueOf(estoque.getQuantidadeInsumo()),
                    String.valueOf(ordemServicoInsumo.getQuantidade()));
        } catch (RuntimeException ignored) {
            // A falha de notificacao nao deve mascarar a insuficiencia de estoque.
        }
    }
}
