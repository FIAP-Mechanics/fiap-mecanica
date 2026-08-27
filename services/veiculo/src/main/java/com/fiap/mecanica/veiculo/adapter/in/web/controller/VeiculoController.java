package com.fiap.mecanica.veiculo.adapter.in.web.controller;

import com.fiap.mecanica.veiculo.adapter.in.web.presenter.VeiculoPresenter;
import com.fiap.mecanica.veiculo.adapter.in.web.request.AtualizarVeiculoRequest;
import com.fiap.mecanica.veiculo.adapter.in.web.request.CadastrarVeiculoRequest;
import com.fiap.mecanica.veiculo.adapter.in.web.response.VeiculoDto;
import com.fiap.mecanica.veiculo.application.command.AtualizarVeiculoCommand;
import com.fiap.mecanica.veiculo.application.port.in.VeiculoUseCase;
import com.fiap.mecanica.veiculo.domain.Veiculo;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fiap.mecanica.veiculo.adapter.in.web.presenter.VeiculoPresenter.toDto;
import static com.fiap.mecanica.veiculo.adapter.in.web.presenter.VeiculoPresenter.toEntity;

@AllArgsConstructor
@RestController
@RequestMapping("/veiculos")
@Secured({"ROLE_ADMIN", "ROLE_ATENDENTE"})
@Tag(name = "Veículos", description = "Operações de gerenciamento de veículos")
public class VeiculoController {

    private final VeiculoUseCase veiculoUseCase;

    @Operation(summary = "Listar veículos", description = "Retorna todos os veículos cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de veículos retornada com sucesso")
    })
    @GetMapping
    public List<VeiculoDto> getList() {
        return veiculoUseCase.buscarTodos().stream()
                .map(VeiculoPresenter::toDto)
                .toList();
    }

    @Operation(summary = "Buscar veículo por ID", description = "Retorna os dados de um veículo pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo encontrado",
                    content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public VeiculoDto get(@Parameter(description = "ID do veículo") @PathVariable Long id) {
        return toDto(veiculoUseCase.buscarVeiculoPorId(id));
    }

    @Operation(summary = "Buscar veículo por Placa", description = "Retorna os dados de um veículo pela sua placa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo encontrado",
                    content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content)
    })
    @GetMapping("/placa/{placa}")
    public VeiculoDto getByPlaca(@Parameter(description = "Placa do veículo") @PathVariable String placa) {
        return toDto(veiculoUseCase.buscarPorPlaca(placa));
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
        return toDto(veiculoUseCase.cadastrarVeiculo(veiculo));
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
        AtualizarVeiculoCommand command = VeiculoPresenter.toCommand(request);
        return toDto(veiculoUseCase.atualizarVeiculo(id, command));
    }

    @Operation(summary = "Excluir veículo", description = "Remove um veículo pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo excluído com sucesso",
                    content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public VeiculoDto delete(@Parameter(description = "ID do veículo") @PathVariable Long id) {
        return toDto(veiculoUseCase.excluirVeiculo(id));
    }

    @Operation(summary = "Reativar veículo", description = "Reativa um veículo previamente desativado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo reativado com sucesso",
                    content = @Content(schema = @Schema(implementation = VeiculoDto.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content)
    })
    @PutMapping("/{id}/reativar")
    public VeiculoDto reativar(@Parameter(description = "ID do veículo") @PathVariable Long id) {
        return toDto(veiculoUseCase.reativarVeiculo(id));
    }
}
