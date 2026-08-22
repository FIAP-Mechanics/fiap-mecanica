package com.fiap.mecanica.atendimento.controller;

import com.fiap.mecanica.atendimento.controller.request.AdicionarItensOrcamentoRequest;
import com.fiap.mecanica.atendimento.controller.request.IniciarAtendimentoRequest;
import com.fiap.mecanica.atendimento.dto.OrdemServicoDto;
import com.fiap.mecanica.atendimento.dto.TempoMedioExecucaoServicoDto;
import com.fiap.mecanica.atendimento.service.OrdemServicoService;
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
    @Secured("ROLE_ATENDENTE")
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

    @Operation(summary = "Lista atendimentos em aberto", description = "Retorna todas as ordens de serviço que não estão entregues ou canceladas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @Secured({"ROLE_ATENDENTE", "ROLE_MECANICO"})
    @GetMapping("/abertos")
    public List<OrdemServicoDto> listarAbertos() {
        return ordemServicoService.listarAtendimentosEmAberto();
    }

    @Operation(summary = "Monitorar tempo medio de execucao dos servicos",
            description = "Retorna o tempo medio entre os status EM_EXECUCAO e FINALIZADA agrupado por servico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Indicadores retornados com sucesso")
    })
    @Secured("ROLE_ADMIN")
    @GetMapping("/relatorios/tempo-medio-servicos")
    public List<TempoMedioExecucaoServicoDto> listarTempoMedioExecucaoServicos() {
        return ordemServicoService.listarTempoMedioExecucaoServicos();
    }

    @Operation(summary = "Iniciar diagnostico", description = "Altera o status da ordem de serviço para EM_DIAGNOSTICO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diagnóstico iniciado com sucesso",
                    content = @Content(schema = @Schema(implementation = OrdemServicoDto.class))),
            @ApiResponse(responseCode = "400", description = "Status incorreto para iniciar diagnóstico", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada", content = @Content)
    })
    @PatchMapping("/{id}/diagnostico/iniciar")
    @Secured("ROLE_MECANICO")
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
    @Secured("ROLE_MECANICO")
    public OrdemServicoDto finalizarDiagnostico(
            @Parameter(description = "ID da ordem de serviço", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id,
            @Valid @RequestBody AdicionarItensOrcamentoRequest request) {
        return ordemServicoService.realizarDiagnostico(id, request.servicos(), request.insumos(), request.observacoes());
    }

    @Operation(summary = "Aprovar ordem de serviço", description = "Aprova o orçamento da ordem de serviço, deduz os itens do estoque e altera o status para EM EXECUÇÃO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem de serviço aprovada com sucesso",
                    content = @Content(schema = @Schema(implementation = OrdemServicoDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, estoque insuficiente ou status incorreto", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada", content = @Content)
    })
    @PostMapping("/{id}/aprovar")
    @Secured("ROLE_ATENDENTE")
    public OrdemServicoDto aprovarOrdemServico(
            @Parameter(description = "ID da ordem de serviço", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        return ordemServicoService.aprovarOrdemServico(id);
    }

    @Operation(summary = "Cancelar ordem de serviço", description = "Cancela a ordem de serviço quando o cliente não aprova o orçamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem de serviço cancelada com sucesso",
                    content = @Content(schema = @Schema(implementation = OrdemServicoDto.class))),
            @ApiResponse(responseCode = "400", description = "Status incorreto para cancelamento", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada", content = @Content)
    })
    @PostMapping("/{id}/cancelar")
    @Secured("ROLE_ATENDENTE")
    public OrdemServicoDto cancelarOrdemServico(
            @Parameter(description = "ID da ordem de serviço", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        return ordemServicoService.cancelarOrdemServico(id);
    }

    @Operation(summary = "Finalizar ordem de serviço", description = "Altera o status da ordem de serviço para FINALIZADA, indicando que o serviço técnico foi concluído")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem de serviço finalizada com sucesso",
                    content = @Content(schema = @Schema(implementation = OrdemServicoDto.class))),
            @ApiResponse(responseCode = "400", description = "Status incorreto para finalização", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada", content = @Content)
    })
    @PostMapping("/{id}/finalizar")
    @Secured("ROLE_MECANICO")
    public OrdemServicoDto finalizarOrdemServico(
            @Parameter(description = "ID da ordem de serviço", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        return ordemServicoService.finalizarOrdemServico(id);
    }

    @Operation(summary = "Entregar veículo/ordem de serviço", description = "Altera o status da ordem de serviço para ENTREGUE, indicando que o veículo foi retirado pelo cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem de serviço entregue com sucesso",
                    content = @Content(schema = @Schema(implementation = OrdemServicoDto.class))),
            @ApiResponse(responseCode = "400", description = "Status incorreto para entrega", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada", content = @Content)
    })
    @PostMapping("/{id}/entregar")
    @Secured("ROLE_ATENDENTE")
    public OrdemServicoDto entregarOrdemServico(
            @Parameter(description = "ID da ordem de serviço", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        return ordemServicoService.entregarVeiculo(id);
    }
}
