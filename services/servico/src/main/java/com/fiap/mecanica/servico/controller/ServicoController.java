package com.fiap.mecanica.servico.controller;

import com.fiap.mecanica.servico.controller.request.AtualizarServicoRequest;
import com.fiap.mecanica.servico.controller.request.CadastrarServicoRequest;
import com.fiap.mecanica.servico.domain.Servico;
import com.fiap.mecanica.servico.dto.ServicoDto;
import com.fiap.mecanica.servico.service.ServicoService;
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

import static com.fiap.mecanica.servico.controller.mapper.ServicoMapper.toDto;
import static com.fiap.mecanica.servico.controller.mapper.ServicoMapper.toEntity;

@AllArgsConstructor
@RestController
@RequestMapping("/servicos")
@Tag(name = "Serviços", description = "Operações de gerenciamento de serviços prestados")
public class ServicoController {

    private final ServicoService service;

    @Operation(summary = "Listar todos os serviços", description = "Retorna todos os tipos de serviços cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de serviços retornada com sucesso")
    })
    @GetMapping
    public List<ServicoDto> getList() {
        return service.buscarTodos().stream()
                .map(com.fiap.mecanica.servico.controller.mapper.ServicoMapper::toDto)
                .toList();
    }

    @Operation(summary = "Buscar serviço por ID", description = "Retorna os dados de um serviço pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço encontrado",
                    content = @Content(schema = @Schema(implementation = ServicoDto.class))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Serviço inativo", content = @Content)
    })
    @GetMapping("/{id}")
    public ServicoDto get(@Parameter(description = "ID do serviço") @PathVariable Long id) {
        return toDto(service.buscarServicoPorId(id));
    }

    @Operation(summary = "Cadastrar serviço", description = "Cria um novo tipo de serviço no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ServicoDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public ServicoDto create(@Valid @RequestBody CadastrarServicoRequest request) {
        Servico servico = toEntity(request);
        return toDto(service.cadastrarServico(servico));
    }

    @Operation(summary = "Atualizar serviço", description = "Atualiza parcialmente os dados de um serviço existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ServicoDto.class))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Serviço inativo", content = @Content)
    })
    @PatchMapping("/{id}")
    public ServicoDto update(
            @Parameter(description = "ID do serviço") @PathVariable Long id,
            @RequestBody AtualizarServicoRequest request) {
        ServicoDto servico = toDto(request);
        return toDto(service.atualizarServico(id, servico));
    }

    @Operation(summary = "Excluir serviço", description = "Inativa um serviço pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço excluído com sucesso",
                    content = @Content(schema = @Schema(implementation = ServicoDto.class))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Serviço já inativo", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ServicoDto delete(@Parameter(description = "ID do serviço") @PathVariable Long id) {
        return toDto(service.excluirServico(id));
    }

    @Operation(summary = "Reativar serviço", description = "Reativa um serviço previamente desativado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço reativado com sucesso",
                    content = @Content(schema = @Schema(implementation = ServicoDto.class))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Serviço já está ativo", content = @Content)
    })
    @PutMapping("/{id}/reativar")
    public ServicoDto reativar(@Parameter(description = "ID do serviço") @PathVariable Long id) {
        return toDto(service.reativarServico(id));
    }
}
