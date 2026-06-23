package com.fiap.mecanica.controller;

import com.fiap.mecanica.controller.request.AtualizarEstoqueRequest;
import com.fiap.mecanica.controller.request.AtualizarInsumoRequest;
import com.fiap.mecanica.controller.request.CadastrarEstoqueRequest;
import com.fiap.mecanica.domain.Estoque;
import com.fiap.mecanica.dto.EstoqueDto;
import com.fiap.mecanica.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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

import static com.fiap.mecanica.controller.mapper.EstoqueMapper.toDto;
import static com.fiap.mecanica.controller.mapper.EstoqueMapper.toEntity;

@AllArgsConstructor
@RestController
@RequestMapping("/estoque")
@Tag(name = "Estoque", description = "Operacoes de gerenciamento do estoque")
public class EstoqueController {
    private final EstoqueService service;

    @Operation(summary = "Listar insumos ativos do estoque")
    @GetMapping
    public List<EstoqueDto> list() {
        return service.listarEstoque().stream()
                .map(estoque -> toDto(estoque))
                .toList();
    }

    @Operation(summary = "Buscar registro do estoque pelo ID do insumo")
    @GetMapping("/{idInsumo}")
    public EstoqueDto get(@Parameter(description = "ID do insumo") @PathVariable Long idInsumo) {
        return toDto(service.buscarPorIdInsumo(idInsumo));
    }

    @Operation(summary = "Registrar insumo no estoque")
    @PostMapping
    public EstoqueDto create(@Valid @RequestBody CadastrarEstoqueRequest request) {
        Estoque estoque = toEntity(request);
        return toDto(service.cadastrarEstoque(estoque));
    }

    @Operation(summary = "Atualizar quantidade do insumo no estoque")
    @PatchMapping("/{idInsumo}")
    public EstoqueDto updateQuantidade(
            @PathVariable Long idInsumo,
            @Valid @RequestBody AtualizarEstoqueRequest request) {
        return toDto(service.atualizarQuantidade(idInsumo, request.quantidade()));
    }

    @Operation(summary = "Atualizar dados do insumo")
    @PatchMapping("/insumo/{idInsumo}")
    public EstoqueDto updateInsumo(
            @PathVariable Long idInsumo,
            @Valid @RequestBody AtualizarInsumoRequest request) {
        return toDto(service.atualizarInsumo(idInsumo, toDto(request)));
    }

    @Operation(summary = "Excluir logicamente registro do estoque")
    @DeleteMapping("/{idInsumo}")
    public EstoqueDto delete(@PathVariable Long idInsumo) {
        return toDto(service.excluirEstoque(idInsumo));
    }

    @Operation(summary = "Reativar registro do estoque")
    @PutMapping("/{idInsumo}")
    public EstoqueDto reativar(@PathVariable Long idInsumo) {
        return toDto(service.reativarEstoque(idInsumo));
    }
}
