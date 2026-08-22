package com.fiap.mecanica.funcionario.controller;

import com.fiap.mecanica.funcionario.controller.request.AtualizarFuncionarioRequest;
import com.fiap.mecanica.funcionario.controller.request.CadastrarFuncionarioRequest;
import com.fiap.mecanica.funcionario.domain.Funcionario;
import com.fiap.mecanica.funcionario.dto.FuncionarioDto;
import com.fiap.mecanica.funcionario.service.FuncionarioService;
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

import java.util.List;

import static com.fiap.mecanica.funcionario.controller.mapper.FuncionarioMapper.toDto;
import static com.fiap.mecanica.funcionario.controller.mapper.FuncionarioMapper.toEntity;

@AllArgsConstructor
@RestController
@RequestMapping("/funcionarios")
@Tag(name = "Funcionários", description = "Operações de gerenciamento de funcionários")
public class FuncionarioController {

    private final FuncionarioService service;

    @Operation(summary = "Listar funcionários", description = "Retorna todos os funcionários cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de funcionários retornada com sucesso")
    })
    @GetMapping
    public List<FuncionarioDto> getList() {
        return service.buscarTodos().stream()
                .map(com.fiap.mecanica.funcionario.controller.mapper.FuncionarioMapper::toDto)
                .toList();
    }

    @Operation(summary = "Buscar funcionário por ID", description = "Retorna os dados de um funcionário pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário encontrado",
                    content = @Content(schema = @Schema(implementation = FuncionarioDto.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Funcionário desativado", content = @Content)
    })
    @GetMapping("/{id}")
    public FuncionarioDto get(@Parameter(description = "ID do funcionário") @PathVariable Long id) {
        return toDto(service.buscarFuncionarioPorId(id));
    }

    @Operation(summary = "Buscar funcionário por e-mail", description = "Retorna os dados de um funcionário pelo seu e-mail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário encontrado",
                    content = @Content(schema = @Schema(implementation = FuncionarioDto.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content)
    })
    @GetMapping("/email/{email}")
    public FuncionarioDto getByEmail(@Parameter(description = "E-mail do funcionário") @PathVariable String email) {
        return toDto(service.buscarPorEmail(email));
    }

    @Operation(summary = "Cadastrar funcionário", description = "Cria um novo funcionário no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = FuncionarioDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content)
    })
    @PostMapping
    public FuncionarioDto create(@Valid @RequestBody CadastrarFuncionarioRequest request) {
        Funcionario funcionario = toEntity(request);
        return toDto(service.cadastrarFuncionario(funcionario));
    }

    @Operation(summary = "Atualizar funcionário", description = "Atualiza parcialmente os dados de um funcionário existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = FuncionarioDto.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Funcionário desativado", content = @Content)
    })
    @PatchMapping("/{id}")
    public FuncionarioDto update(
            @Parameter(description = "ID do funcionário") @PathVariable Long id,
            @RequestBody AtualizarFuncionarioRequest request) {
        FuncionarioDto funcionario = toDto(request);
        return toDto(service.atualizarFuncionario(id, funcionario));
    }

    @Operation(summary = "Excluir funcionário", description = "Inativa um funcionário pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário excluído com sucesso",
                    content = @Content(schema = @Schema(implementation = FuncionarioDto.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Funcionário já desativado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public FuncionarioDto delete(@Parameter(description = "ID do funcionário") @PathVariable Long id) {
        return toDto(service.excluirFuncionario(id));
    }

    @Operation(summary = "Reativar funcionário", description = "Reativa um funcionário previamente desativado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário reativado com sucesso",
                    content = @Content(schema = @Schema(implementation = FuncionarioDto.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Funcionário já está ativo", content = @Content)
    })
    @PutMapping("/{id}/reativar")
    public FuncionarioDto reativar(@Parameter(description = "ID do funcionário") @PathVariable Long id) {
        return toDto(service.reativarFuncionario(id));
    }
}
