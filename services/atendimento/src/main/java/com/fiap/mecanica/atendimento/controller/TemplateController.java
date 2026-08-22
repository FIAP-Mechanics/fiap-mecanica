package com.fiap.mecanica.atendimento.controller;

import com.fiap.mecanica.atendimento.controller.mapper.TemplateMapper;
import com.fiap.mecanica.atendimento.controller.request.AtualizarTemplateRequest;
import com.fiap.mecanica.atendimento.controller.request.CriarTemplateRequest;
import com.fiap.mecanica.atendimento.dto.TemplateDto;
import com.fiap.mecanica.atendimento.infra.enums.CodigoTemplate;
import com.fiap.mecanica.atendimento.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/templates")
@Secured({"ROLE_ADMIN"})
@Tag(name = "Templates de Notificação", description = "Operações de gerenciamento de templates de notificação por e-mail")
public class TemplateController {

    private final TemplateService service;

    @Operation(summary = "Listar templates", description = "Retorna a lista de todos os templates de notificação cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = TemplateDto.class)))
    })
    @GetMapping
    public List<TemplateDto> getList() {
        return service.buscarTodos().stream()
                .map(TemplateMapper::toDto)
                .toList();
    }

    @Operation(summary = "Buscar template por código", description = "Retorna os dados de um template de notificação pelo seu código")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template encontrado",
                    content = @Content(schema = @Schema(implementation = TemplateDto.class))),
            @ApiResponse(responseCode = "404", description = "Template não encontrado", content = @Content)
    })
    @GetMapping("/{codigo}")
    public TemplateDto get(
            @Parameter(description = "Código do template de notificação") @PathVariable CodigoTemplate codigo
    ) {
        return TemplateMapper.toDto(service.buscarPorCodigo(codigo));
    }

    @Operation(summary = "Cadastrar template", description = "Cria um novo template de notificação no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = TemplateDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Já existe um template com esse código", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateDto create(@Valid @RequestBody CriarTemplateRequest request) {
        return TemplateMapper.toDto(service.cadastrar(TemplateMapper.toEntity(request)));
    }

    @Operation(summary = "Atualizar template", description = "Atualiza o conteúdo de um template de notificação existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = TemplateDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Template não encontrado", content = @Content)
    })
    @PutMapping("/{codigo}")
    public TemplateDto update(
            @Parameter(description = "Código do template de notificação") @PathVariable CodigoTemplate codigo,
            @Valid @RequestBody AtualizarTemplateRequest request
    ) {
        TemplateDto dto = TemplateDto.builder()
                .conteudo(request.conteudo())
                .build();
        return TemplateMapper.toDto(service.atualizar(codigo, dto));
    }

    @Operation(summary = "Excluir template", description = "Remove permanentemente um template de notificação do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Template excluído com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Template não encontrado", content = @Content)
    })
    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Código do template de notificação") @PathVariable CodigoTemplate codigo
    ) {
        service.deletar(codigo);
    }
}
