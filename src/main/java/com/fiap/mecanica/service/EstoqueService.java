package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Estoque;
import com.fiap.mecanica.domain.Insumo;
import com.fiap.mecanica.domain.OrdemServicoInsumo;
import com.fiap.mecanica.dto.InsumoDto;
import com.fiap.mecanica.exception.EstoqueInativoException;
import com.fiap.mecanica.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.exception.EstoqueJaAtivoException;
import com.fiap.mecanica.exception.EstoqueNotFound;
import com.fiap.mecanica.infra.configs.enums.CodigoTemplate;
import com.fiap.mecanica.repository.EstoqueRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
@Service
public class EstoqueService {
    private final EstoqueRepository repository;
    private final NotificationService notificationService;

    public Estoque cadastrarEstoque(Estoque estoque) {
        return repository.save(estoque);
    }

    public Estoque buscarPorIdInsumo(Long idInsumo) {
        Estoque estoque = buscarRegistro(idInsumo);
        if (!estoque.isAtivo()) throw new EstoqueInativoException(idInsumo);
        return estoque;
    }

    public List<Estoque> listarEstoque() {
        return repository.findAllByAtivoTrue();
    }

    public Estoque atualizarQuantidade(Long idInsumo, Long quantidade) {
        Estoque estoque = buscarPorIdInsumo(idInsumo);
        estoque.setQuantidadeInsumo(quantidade);
        return repository.save(estoque);
    }

    public Estoque atualizarInsumo(Long idInsumo, InsumoDto insumoDto) {
        Estoque estoque = buscarPorIdInsumo(idInsumo);
        Insumo insumo = estoque.getInsumo();
        atualizaSeExistente(insumoDto.nome(), insumo::setNome);
        atualizaSeExistente(insumoDto.precoUnitario(), insumo::setPrecoUnitario);
        return repository.save(estoque);
    }

    public Estoque excluirEstoque(Long idInsumo) {
        Estoque estoque = buscarPorIdInsumo(idInsumo);
        estoque.setAtivo(false);
        return repository.save(estoque);
    }

    @Transactional
    public void deduzirEstoque(List<OrdemServicoInsumo> insumos) {
        for (OrdemServicoInsumo osInsumo : insumos) {
            Estoque estoque = buscarPorIdInsumo(osInsumo.getInsumo().getId());
            if (estoque.getQuantidadeInsumo() < osInsumo.getQuantidade()) {
                notificarNecessidadeReposicao(estoque, osInsumo);
                throw new EstoqueInsuficienteException(
                        osInsumo.getInsumo().getNome(),
                        estoque.getQuantidadeInsumo(),
                        osInsumo.getQuantidade()
                );
            }
            estoque.setQuantidadeInsumo(estoque.getQuantidadeInsumo() - osInsumo.getQuantidade());
            repository.save(estoque);
        }
    }

    public Estoque reativarEstoque(Long idInsumo) {
        Estoque estoque = buscarRegistro(idInsumo);
        if (estoque.isAtivo()) throw new EstoqueJaAtivoException(idInsumo);
        estoque.setAtivo(true);
        return repository.save(estoque);
    }

    private Estoque buscarRegistro(Long idInsumo) {
        return repository.findByInsumoId(idInsumo).orElseThrow(() -> new EstoqueNotFound(idInsumo));
    }

    private <T> void atualizaSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) {
            setter.accept(valor);
        }
    }

    private void notificarNecessidadeReposicao(Estoque estoque, OrdemServicoInsumo osInsumo) {
        try {
            String nomeInsumo = osInsumo.getInsumo().getNome() != null
                    ? osInsumo.getInsumo().getNome()
                    : estoque.getInsumo().getNome();
            notificationService.notificarFuncionarios(
                    CodigoTemplate.REPOSICAO_ESTOQUE,
                    nomeInsumo,
                    String.valueOf(estoque.getQuantidadeInsumo()),
                    String.valueOf(osInsumo.getQuantidade())
            );
        } catch (RuntimeException ignored) {
            // A notificacao de reposicao nao deve mascarar a regra de estoque insuficiente.
        }
    }
}
