package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Estoque;
import com.fiap.mecanica.domain.Insumo;
import com.fiap.mecanica.dto.InsumoDto;
import com.fiap.mecanica.exception.EstoqueInativoException;
import com.fiap.mecanica.exception.EstoqueJaAtivoException;
import com.fiap.mecanica.exception.EstoqueNotFound;
import com.fiap.mecanica.repository.EstoqueRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
@Service
public class EstoqueService {
    private final EstoqueRepository repository;

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
}
