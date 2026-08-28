package com.fiap.mecanica.estoque.adapter.in.web.controller;

import com.fiap.mecanica.estoque.adapter.in.web.presenter.EstoquePresenter;
import com.fiap.mecanica.estoque.adapter.in.web.request.AtualizarEstoqueRequest;
import com.fiap.mecanica.estoque.adapter.in.web.request.AtualizarInsumoRequest;
import com.fiap.mecanica.estoque.adapter.in.web.request.CadastrarEstoqueRequest;
import com.fiap.mecanica.estoque.adapter.in.web.request.DeduzirEstoqueItemRequest;
import com.fiap.mecanica.estoque.adapter.in.web.response.EstoqueDto;
import com.fiap.mecanica.estoque.application.port.in.EstoqueUseCase;
import com.fiap.mecanica.estoque.domain.Estoque;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.fiap.mecanica.estoque.adapter.in.web.presenter.EstoquePresenter.toDto;
import static com.fiap.mecanica.estoque.adapter.in.web.presenter.EstoquePresenter.toEntity;

@AllArgsConstructor
@RestController
@RequestMapping("/estoque")
@Tag(name = "Estoque", description = "Operacoes de gerenciamento do estoque")
public class EstoqueController {
    private final EstoqueUseCase estoqueUseCase;

    @Operation(summary = "Listar insumos ativos do estoque")
    @GetMapping
    public List<EstoqueDto> list() {
        return estoqueUseCase.listarEstoque().stream()
                .map(EstoquePresenter::toDto)
                .toList();
    }

    @Operation(summary = "Buscar registro do estoque pelo ID do insumo")
    @GetMapping("/{idInsumo}")
    public EstoqueDto get(@Parameter(description = "ID do insumo") @PathVariable Long idInsumo) {
        return toDto(estoqueUseCase.buscarPorIdInsumo(idInsumo));
    }

    @Operation(summary = "Registrar insumo no estoque")
    @Secured({"ROLE_ADMIN", "ROLE_ATENDENTE", "ROLE_MECANICO"})
    @PostMapping
    public EstoqueDto create(@Valid @RequestBody CadastrarEstoqueRequest request) {
        Estoque estoque = toEntity(request);
        return toDto(estoqueUseCase.cadastrarEstoque(estoque));
    }

    @Operation(summary = "Atualizar quantidade do insumo no estoque")
    @Secured({"ROLE_ADMIN", "ROLE_ATENDENTE", "ROLE_MECANICO"})
    @PutMapping("/{idInsumo}")
    public EstoqueDto updateQuantidade(
            @PathVariable Long idInsumo,
            @Valid @RequestBody AtualizarEstoqueRequest request) {
        return toDto(estoqueUseCase.atualizarQuantidade(idInsumo, request.quantidade()));
    }

    @Operation(summary = "Atualizar dados do insumo")
    @Secured({"ROLE_ADMIN", "ROLE_ATENDENTE", "ROLE_MECANICO"})
    @PatchMapping("/insumo/{idInsumo}")
    public EstoqueDto updateInsumo(
            @PathVariable Long idInsumo,
            @Valid @RequestBody AtualizarInsumoRequest request) {
        return toDto(estoqueUseCase.atualizarInsumo(idInsumo, EstoquePresenter.toCommand(request)));
    }

    @Operation(summary = "Excluir logicamente registro do estoque")
    @Secured({"ROLE_ADMIN", "ROLE_ATENDENTE", "ROLE_MECANICO"})
    @DeleteMapping("/{idInsumo}")
    public EstoqueDto delete(@PathVariable Long idInsumo) {
        return toDto(estoqueUseCase.excluirEstoque(idInsumo));
    }

    @Operation(summary = "Reativar registro do estoque")
    @Secured({"ROLE_ADMIN", "ROLE_ATENDENTE", "ROLE_MECANICO"})
    @PutMapping("/{idInsumo}/reativar")
    public EstoqueDto reativar(@PathVariable Long idInsumo) {
        return toDto(estoqueUseCase.reativarEstoque(idInsumo));
    }

    @Operation(summary = "Deduzir itens do estoque", description = "Usado por outros microsservicos (ex.: atendimento) para abater quantidades do estoque")
    @Secured({"ROLE_ADMIN", "ROLE_ATENDENTE", "ROLE_MECANICO"})
    @PostMapping("/deduzir")
    public void deduzir(@Valid @RequestBody List<DeduzirEstoqueItemRequest> itens) {
        estoqueUseCase.deduzirEstoque(EstoquePresenter.toCommands(itens));
    }
}
