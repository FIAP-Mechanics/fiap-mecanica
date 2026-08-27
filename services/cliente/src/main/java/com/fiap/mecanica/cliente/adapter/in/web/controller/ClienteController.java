package com.fiap.mecanica.cliente.adapter.in.web.controller;

import com.fiap.mecanica.cliente.adapter.in.web.presenter.ClientePresenter;
import com.fiap.mecanica.cliente.adapter.in.web.request.AtualizarClienteRequest;
import com.fiap.mecanica.cliente.adapter.in.web.request.CadastrarClienteRequest;
import com.fiap.mecanica.cliente.adapter.in.web.response.ClienteDto;
import com.fiap.mecanica.cliente.adapter.in.web.response.VeiculoDto;
import com.fiap.mecanica.cliente.application.command.AtualizarClienteCommand;
import com.fiap.mecanica.cliente.application.port.in.ClienteUseCase;
import com.fiap.mecanica.cliente.application.port.in.VinculoVeiculoUseCase;
import com.fiap.mecanica.cliente.domain.Cliente;
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

import static com.fiap.mecanica.cliente.adapter.in.web.presenter.ClientePresenter.toDto;
import static com.fiap.mecanica.cliente.adapter.in.web.presenter.ClientePresenter.toEntity;

@AllArgsConstructor
@RestController
@RequestMapping("/cliente")
@Secured({"ROLE_ADMIN", "ROLE_ATENDENTE"})
@Tag(name = "Clientes", description = "Operações de gerenciamento de clientes")
public class ClienteController {

    private final ClienteUseCase clienteUseCase;
    private final VinculoVeiculoUseCase vinculoVeiculoUseCase;

    @Operation(summary = "Buscar lista de clientes", description = "Retorna lista de clientes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @GetMapping
    public List<ClienteDto> getList() {
        List<Cliente> clientes = clienteUseCase.buscarClientes();
        return clientes.stream()
                .map(ClientePresenter::toDto)
                .toList();
    }

    @Operation(summary = "Buscar cliente por CPF/CNPJ", description = "Identifica o cliente pelo documento informado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @GetMapping("/documento")
    public ClienteDto getByDocumento(
            @Parameter(description = "CPF ou CNPJ do cliente") @RequestParam String documento
    ) {
        return toDto(clienteUseCase.buscarClientePorDocumento(documento));
    }

    @Operation(summary = "Buscar cliente por ID", description = "Retorna os dados de um cliente pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ClienteDto get(
            @Parameter(description = "ID do cliente") @PathVariable Long id
    ) {
        return toDto(clienteUseCase.buscarClientePorId(id));
    }

    @Operation(summary = "Cadastrar cliente", description = "Cria um novo cliente no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Já existe um cliente com esse documento", content = @Content)
    })
    @PostMapping
    public ClienteDto create(@Valid @RequestBody CadastrarClienteRequest request) {
        Cliente cliente = toEntity(request);
        return toDto(clienteUseCase.cadastrarCliente(cliente));
    }

    @Operation(summary = "Atualizar cliente", description = "Atualiza todos os dados de um cliente existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @PatchMapping("/{id}")
    public ClienteDto update(
            @Parameter(description = "ID do cliente") @PathVariable Long id,
            @RequestBody AtualizarClienteRequest request) {
        AtualizarClienteCommand command = ClientePresenter.toCommand(request);
        return toDto(clienteUseCase.atualizarCliente(id, command));
    }

    @Operation(summary = "Vincular veículo ao cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Veículo vinculado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente ou veículo não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Vínculo já existe", content = @Content)
    })
    @PutMapping("/{idCliente}/veiculo/{idVeiculo}")
    public void vincularClienteVeiculo(
            @Parameter(description = "ID do cliente") @PathVariable Long idCliente,
            @Parameter(description = "ID do veiculo") @PathVariable Long idVeiculo,
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) Integer ano
    ) {
        vinculoVeiculoUseCase.vincularVeiculo(idCliente, idVeiculo, placa, marca, modelo, ano);
    }

    @Operation(summary = "Listar veículos do cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de veículos retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @GetMapping("/{id}/veiculos")
    public List<VeiculoDto> listarVeiculos(
            @Parameter(description = "ID do cliente") @PathVariable Long id) {
        return vinculoVeiculoUseCase.listarVeiculosDoCliente(id).stream()
                .map(ClientePresenter::toDto)
                .toList();
    }
}
