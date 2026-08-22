package com.fiap.mecanica.adapter.in.web.controller;

import com.fiap.mecanica.adapter.in.web.presenter.FuncionarioPresenter;
import com.fiap.mecanica.adapter.in.web.request.AtualizarFuncionarioRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarFuncionarioRequest;
import com.fiap.mecanica.adapter.in.web.response.FuncionarioDto;
import com.fiap.mecanica.application.port.in.FuncionarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@AllArgsConstructor
@RestController
@RequestMapping("/funcionarios")
@Tag(name = "Funcionários", description = "Operações de gerenciamento de funcionários")
public class FuncionarioController {

    private final FuncionarioUseCase funcionarios;

    @Operation(summary = "Buscar funcionário por ID", description = "Retorna os dados de um funcionário pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário encontrado", content = @Content(schema = @Schema(implementation = FuncionarioDto.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Funcionário desativado", content = @Content)
    })
    @GetMapping("/{id}")
    public FuncionarioDto get(@Parameter(description = "ID do funcionário") @PathVariable Long id) {
        return FuncionarioPresenter.toDto(funcionarios.buscarFuncionarioPorId(id));
    }

    @Operation(summary = "Cadastrar funcionário", description = "Cria um novo funcionário no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário cadastrado com sucesso", content = @Content(schema = @Schema(implementation = FuncionarioDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public FuncionarioDto create(@Valid @RequestBody CadastrarFuncionarioRequest request) {
        return FuncionarioPresenter.toDto(
                funcionarios.cadastrarFuncionario(FuncionarioPresenter.toEntity(request)));
    }

    @Operation(summary = "Atualizar funcionário", description = "Atualiza parcialmente os dados de um funcionário existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário atualizado com sucesso", content = @Content(schema = @Schema(implementation = FuncionarioDto.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Funcionário desativado", content = @Content)
    })
    @PatchMapping("/{id}")
    public FuncionarioDto update(
            @Parameter(description = "ID do funcionário") @PathVariable Long id,
            @RequestBody AtualizarFuncionarioRequest request) {
        return FuncionarioPresenter.toDto(
                funcionarios.atualizarFuncionario(id, FuncionarioPresenter.toCommand(request)));
    }

    @Operation(summary = "Excluir funcionário", description = "Inativa um funcionário pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário excluído com sucesso", content = @Content(schema = @Schema(implementation = FuncionarioDto.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Funcionário já desativado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public FuncionarioDto delete(@Parameter(description = "ID do funcionário") @PathVariable Long id) {
        return FuncionarioPresenter.toDto(funcionarios.excluirFuncionario(id));
    }

    @Operation(summary = "Reativar funcionário", description = "Reativa um funcionário previamente desativado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário reativado com sucesso", content = @Content(schema = @Schema(implementation = FuncionarioDto.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Funcionário já está ativo", content = @Content)
    })
    @PutMapping("/{id}/reativar")
    public FuncionarioDto reativar(@Parameter(description = "ID do funcionário") @PathVariable Long id) {
        return FuncionarioPresenter.toDto(funcionarios.reativarFuncionario(id));
    }
}
