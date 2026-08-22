package com.fiap.mecanica.adapter.in.web.controller;

import com.fiap.mecanica.adapter.in.web.presenter.VeiculoPresenter;
import com.fiap.mecanica.adapter.in.web.request.AtualizarVeiculoRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarVeiculoRequest;
import com.fiap.mecanica.adapter.in.web.response.VeiculoDto;
import com.fiap.mecanica.application.port.in.VeiculoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@AllArgsConstructor
@RestController
@RequestMapping("/veiculos")
@Secured("ROLE_ATENDENTE")
@Tag(name = "Veículos", description = "Operações de gerenciamento de veículos")
public class VeiculoController {

    private final VeiculoUseCase veiculos;

    @Operation(summary = "Buscar veículo por ID", description = "Retorna os dados de um veículo pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo encontrado", content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public VeiculoDto get(@Parameter(description = "ID do veículo") @PathVariable Long id) {
        return VeiculoPresenter.toDto(veiculos.buscarVeiculoPorId(id));
    }

    @Operation(summary = "Cadastrar veículo", description = "Cria um novo veículo no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo cadastrado com sucesso", content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public VeiculoDto create(@Valid @RequestBody CadastrarVeiculoRequest request) {
        return VeiculoPresenter.toDto(veiculos.cadastrarVeiculo(VeiculoPresenter.toEntity(request)));
    }

    @Operation(summary = "Atualizar veículo", description = "Atualiza parcialmente os dados de um veículo existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso", content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content)
    })
    @PatchMapping("/{id}")
    public VeiculoDto update(
            @Parameter(description = "ID do veículo") @PathVariable Long id,
            @RequestBody AtualizarVeiculoRequest request) {
        return VeiculoPresenter.toDto(veiculos.atualizarVeiculo(id, VeiculoPresenter.toCommand(request)));
    }

    @Operation(summary = "Excluir veículo", description = "Remove um veículo pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo excluído com sucesso", content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public VeiculoDto delete(@Parameter(description = "ID do veículo") @PathVariable Long id) {
        return VeiculoPresenter.toDto(veiculos.excluirVeiculo(id));
    }

    @Operation(summary = "Reativar veículo", description = "Reativa um veículo previamente desativado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo reativado com sucesso", content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content)
    })
    @PutMapping("/{id}/reativar")
    public VeiculoDto reativar(@Parameter(description = "ID do veículo") @PathVariable Long id) {
        return VeiculoPresenter.toDto(veiculos.reativarVeiculo(id));
    }
}
