package com.fiap.mecanica.adapter.in.web.controller;

import com.fiap.mecanica.adapter.in.web.presenter.EstoquePresenter;
import com.fiap.mecanica.adapter.in.web.request.AtualizarEstoqueRequest;
import com.fiap.mecanica.adapter.in.web.request.AtualizarInsumoRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarEstoqueRequest;
import com.fiap.mecanica.adapter.in.web.response.EstoqueDto;
import com.fiap.mecanica.application.port.in.EstoqueUseCase;
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

@AllArgsConstructor
@RestController
@RequestMapping("/estoque")
@Secured({"ROLE_ATENDENTE", "ROLE_MECANICO"})
@Tag(name = "Estoque", description = "Operacoes de gerenciamento do estoque")
public class EstoqueController {

    private final EstoqueUseCase estoque;

    @Operation(summary = "Listar insumos ativos do estoque")
    @GetMapping
    public List<EstoqueDto> list() {
        return estoque.listarEstoque().stream().map(EstoquePresenter::toDto).toList();
    }

    @Operation(summary = "Buscar registro do estoque pelo ID do insumo")
    @GetMapping("/{idInsumo}")
    public EstoqueDto get(@Parameter(description = "ID do insumo") @PathVariable Long idInsumo) {
        return EstoquePresenter.toDto(estoque.buscarPorIdInsumo(idInsumo));
    }

    @Operation(summary = "Registrar insumo no estoque")
    @PostMapping
    public EstoqueDto create(@Valid @RequestBody CadastrarEstoqueRequest request) {
        return EstoquePresenter.toDto(estoque.cadastrarEstoque(EstoquePresenter.toEntity(request)));
    }

    @Operation(summary = "Atualizar quantidade do insumo no estoque")
    @PutMapping("/{idInsumo}")
    public EstoqueDto updateQuantidade(
            @PathVariable Long idInsumo,
            @Valid @RequestBody AtualizarEstoqueRequest request) {
        return EstoquePresenter.toDto(estoque.atualizarQuantidade(idInsumo, request.quantidade()));
    }

    @Operation(summary = "Atualizar dados do insumo")
    @PatchMapping("/insumo/{idInsumo}")
    public EstoqueDto updateInsumo(
            @PathVariable Long idInsumo,
            @Valid @RequestBody AtualizarInsumoRequest request) {
        return EstoquePresenter.toDto(estoque.atualizarInsumo(idInsumo, EstoquePresenter.toCommand(request)));
    }

    @Operation(summary = "Excluir logicamente registro do estoque")
    @DeleteMapping("/{idInsumo}")
    public EstoqueDto delete(@PathVariable Long idInsumo) {
        return EstoquePresenter.toDto(estoque.excluirEstoque(idInsumo));
    }

    @Operation(summary = "Reativar registro do estoque")
    @PutMapping("/{idInsumo}/reativar")
    public EstoqueDto reativar(@PathVariable Long idInsumo) {
        return EstoquePresenter.toDto(estoque.reativarEstoque(idInsumo));
    }
}
