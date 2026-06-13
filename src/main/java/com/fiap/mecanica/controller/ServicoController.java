package com.fiap.mecanica.controller;

import com.fiap.mecanica.controller.request.AtualizarServicoRequest;
import com.fiap.mecanica.controller.request.CadastrarServicoRequest;
import com.fiap.mecanica.domain.Servico;
import com.fiap.mecanica.dto.ServicoDto;
import com.fiap.mecanica.service.ServicoService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import static com.fiap.mecanica.controller.mapper.ServicoMapper.toDto;
import static com.fiap.mecanica.controller.mapper.ServicoMapper.toEntity;

@AllArgsConstructor
@RestController
@RequestMapping("/servicos")
@Tag(name = "Servicos", description = "Operacoes de gerenciamento de servicos")
public class ServicoController {

    private final ServicoService service;

    @Operation(summary = "Buscar servico por ID", description = "Retorna os dados de um servico pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servico encontrado", content = @Content(schema = @Schema(implementation = ServicoDto.class))),
            @ApiResponse(responseCode = "404", description = "Servico nao encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Servico desativado", content = @Content)
    })
    @GetMapping("/{idServico}")
    public ServicoDto get(@Parameter(description = "ID do servico") @PathVariable Long idServico) {
        return toDto(service.buscarServicoPorId(idServico));
    }

    @Operation(summary = "Cadastrar servico", description = "Cria um novo servico no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servico cadastrado com sucesso", content = @Content(schema = @Schema(implementation = ServicoDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content)
    })
    @PostMapping
    public ServicoDto create(@Valid @RequestBody CadastrarServicoRequest request) {
        Servico servico = toEntity(request);
        return toDto(service.cadastrarServico(servico));
    }

    @Operation(summary = "Atualizar servico", description = "Atualiza parcialmente os dados de um servico existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servico atualizado com sucesso", content = @Content(schema = @Schema(implementation = ServicoDto.class))),
            @ApiResponse(responseCode = "404", description = "Servico nao encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Servico desativado", content = @Content)
    })
    @PatchMapping("/{idServico}")
    public ServicoDto update(
            @Parameter(description = "ID do servico") @PathVariable Long idServico,
            @RequestBody AtualizarServicoRequest request) {
        ServicoDto servico = toDto(request);
        return toDto(service.atualizarServico(idServico, servico));
    }

    @Operation(summary = "Excluir servico", description = "Inativa um servico pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servico excluido com sucesso", content = @Content(schema = @Schema(implementation = ServicoDto.class))),
            @ApiResponse(responseCode = "404", description = "Servico nao encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Servico ja desativado", content = @Content)
    })
    @DeleteMapping("/{idServico}")
    public ServicoDto delete(@Parameter(description = "ID do servico") @PathVariable Long idServico) {
        return toDto(service.excluirServico(idServico));
    }

    @Operation(summary = "Reativar servico", description = "Reativa um servico previamente desativado")
    @PutMapping("/{idServico}/reativar")
    public ServicoDto reativar(@Parameter(description = "ID do servico") @PathVariable Long idServico) {
        return toDto(service.reativarServico(idServico));
    }
}
