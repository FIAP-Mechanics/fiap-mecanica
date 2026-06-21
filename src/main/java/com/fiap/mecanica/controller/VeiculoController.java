package com.fiap.mecanica.controller;

import com.fiap.mecanica.controller.request.AtualizarVeiculoRequest;
import com.fiap.mecanica.controller.request.CadastrarVeiculoRequest;
import com.fiap.mecanica.domain.Veiculo;
import com.fiap.mecanica.dto.VeiculoDto;
import com.fiap.mecanica.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.fiap.mecanica.controller.mapper.VeiculoMapper.toDto;
import static com.fiap.mecanica.controller.mapper.VeiculoMapper.toEntity;

@AllArgsConstructor
@RestController
@RequestMapping("/veiculos")
@Tag(name = "Veículos", description = "Operações de gerenciamento de veículos")
public class VeiculoController {

    private final VeiculoService service;

    @Operation(summary = "Buscar veículo por ID", description = "Retorna os dados de um veículo pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo encontrado",
                    content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public VeiculoDto get(@Parameter(description = "ID do veículo") @PathVariable Long id) {
        return toDto(service.buscarVeiculoPorId(id));
    }

    @Operation(summary = "Cadastrar veículo", description = "Cria um novo veículo no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public VeiculoDto create(@Valid @RequestBody CadastrarVeiculoRequest request) {
        Veiculo veiculo = toEntity(request);
        return toDto(service.cadastrarVeiculo(veiculo));
    }

    @Operation(summary = "Atualizar veículo", description = "Atualiza parcialmente os dados de um veículo existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content)
    })
    @PatchMapping("/{id}")
    public VeiculoDto update(
            @Parameter(description = "ID do veículo") @PathVariable Long id,
            @RequestBody AtualizarVeiculoRequest request) {
        VeiculoDto veiculo = toDto(request);
        return toDto(service.atualizarVeiculo(id, veiculo));
    }

    @Operation(summary = "Excluir veículo", description = "Remove um veículo pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo excluído com sucesso",
                    content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public VeiculoDto delete(@Parameter(description = "ID do veículo") @PathVariable Long id) {
        return toDto(service.excluirVeiculo(id));
    }

    @Operation(summary = "Reativar veículo", description = "Reativa um veículo previamente desativado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo reativado com sucesso",
                    content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
                    content = @Content)
    })
    @PutMapping("/{id}/reativar")
    public VeiculoDto reativar(
            @Parameter(description = "ID do veículo")
            @PathVariable Long id) {

        return toDto(service.reativarVeiculo(id));
    }
}