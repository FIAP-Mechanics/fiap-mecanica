package com.fiap.mecanica.controller;

import com.fiap.mecanica.controller.request.AdicionarItensOrcamentoRequest;
import com.fiap.mecanica.controller.request.IniciarAtendimentoRequest;
import com.fiap.mecanica.dto.OrdemServicoDto;
import com.fiap.mecanica.service.OrdemServicoService;
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

@AllArgsConstructor
@RestController
@RequestMapping("/atendimento")
@Tag(name = "Atendimento", description = "Operações de gerenciamento de ordens de serviço")
public class AtendimentoController {

    private final OrdemServicoService ordemServicoService;

    @Operation(summary = "Iniciar atendimento", description = "Inicia um atendimento criando uma nova ordem de serviço para o cliente e veículo informados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atendimento iniciado com sucesso",
                    content = @Content(schema = @Schema(implementation = OrdemServicoDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente ou veículo não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Veículo inativo", content = @Content)
    })
    @PostMapping("/iniciar")
    public OrdemServicoDto iniciarAtendimento(@Valid @RequestBody IniciarAtendimentoRequest request) {
        return ordemServicoService.iniciarAtendimento(request.cliente(), request.veiculo(), request.relatoCliente(), request.servicos(), request.insumos());
    }

    @Operation(summary = "Buscar ordem de serviço", description = "Retorna os dados completos de uma ordem de serviço pelo seu ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem de serviço encontrada",
                    content = @Content(schema = @Schema(implementation = OrdemServicoDto.class))),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public OrdemServicoDto buscarPorId(
            @Parameter(description = "ID da ordem de serviço", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        return ordemServicoService.buscarPorId(id);
    }

    @Operation(summary = "Lista atendimentos em aberto", description = "Retorna todas as ordens de serviço que não estão com status FINALIZADA")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping("/abertos")
    public List<OrdemServicoDto> listarAbertos() {
        return ordemServicoService.listarAtendimentosEmAberto();
    }

    @Operation(summary = "Iniciar atendimento", description = "Inicia um atendimento criando uma nova ordem de serviço para o cliente e veículo informados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atendimento iniciado com sucesso",
                    content = @Content(schema = @Schema(implementation = OrdemServicoDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente ou veículo não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Veículo inativo", content = @Content)
    })
    @PatchMapping("/{id}/diagnostico/iniciar")
    public OrdemServicoDto iniciarDiagnostico(@Parameter(description = "ID da ordem de serviço", example = "550e8400-e29b-41d4-a716-446655440000")
                                              @PathVariable String id) {
        return ordemServicoService.iniciarDiagnostico(id);
    }

    @Operation(summary = "Adicionar itens ao orçamento", description = "Adiciona serviços e insumos ao orçamento da ordem de serviço, desde que esteja em diagnóstico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itens adicionados com sucesso",
                    content = @Content(schema = @Schema(implementation = OrdemServicoDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou status incorreto", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço, serviço ou insumo não encontrado", content = @Content)
    })
    @PostMapping("/{id}/diagnostico")
    public OrdemServicoDto adicionarItens(
            @Parameter(description = "ID da ordem de serviço", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id,
            @Valid @RequestBody AdicionarItensOrcamentoRequest request) {
        return ordemServicoService.adicionarItens(id, request.servicos(), request.insumos(), request.observacoes());
    }
}
