package com.fiap.mecanica.adapter.in.web.controller;

import com.fiap.mecanica.adapter.in.web.presenter.ClientePresenter;
import com.fiap.mecanica.adapter.in.web.presenter.VeiculoPresenter;
import com.fiap.mecanica.adapter.in.web.request.AtualizarClienteRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarClienteRequest;
import com.fiap.mecanica.adapter.in.web.response.ClienteDto;
import com.fiap.mecanica.adapter.in.web.response.VeiculoDto;
import com.fiap.mecanica.application.port.in.ClienteUseCase;
import com.fiap.mecanica.application.port.in.VinculoVeiculoUseCase;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/cliente")
@Secured("ROLE_ATENDENTE")
@Tag(name = "Clientes", description = "Operações de gerenciamento de clientes")
public class ClienteController {

    private final ClienteUseCase clientes;
    private final VinculoVeiculoUseCase vinculos;

    @Operation(summary = "Buscar lista de clientes", description = "Retorna lista de clientes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado", content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "404", description = "Clinete não encontrado", content = @Content)
    })
    @GetMapping
    public List<ClienteDto> getList() {
        return clientes.buscarClientes().stream().map(ClientePresenter::toDto).toList();
    }

    @Operation(summary = "Buscar cliente por CPF/CNPJ", description = "Identifica o cliente pelo documento informado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado", content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "404", description = "Cliente nÃ£o encontrado", content = @Content)
    })
    @GetMapping("/documento")
    public ClienteDto getByDocumento(
            @Parameter(description = "CPF ou CNPJ do cliente") @RequestParam String documento) {
        return ClientePresenter.toDto(clientes.buscarClientePorDocumento(documento));
    }

    @Operation(summary = "Buscar cliente por ID", description = "Retorna os dados de um cliente pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado", content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "404", description = "Clinete não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ClienteDto get(@Parameter(description = "ID do cliente") @PathVariable Long id) {
        return ClientePresenter.toDto(clientes.buscarClientePorId(id));
    }

    @Operation(summary = "Cadastrar cliente", description = "Cria um novo cliente no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente cadastrado com sucesso", content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Já existe um cliente com esse documento", content = @Content)
    })
    @PostMapping
    public ClienteDto create(@Valid @RequestBody CadastrarClienteRequest request) {
        return ClientePresenter.toDto(clientes.cadastrarCliente(ClientePresenter.toEntity(request)));
    }

    @Operation(summary = "Atualizar cliente", description = "Atualiza todos os dados de um cliente existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso", content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ClienteDto update(
            @Parameter(description = "ID do cliente") @PathVariable Long id,
            @RequestBody AtualizarClienteRequest request) {
        return ClientePresenter.toDto(clientes.atualizarCliente(id, ClientePresenter.toCommand(request)));
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
            @Parameter(description = "ID do veiculo") @PathVariable Long idVeiculo) {
        vinculos.vincularVeiculo(idCliente, idVeiculo);
    }

    @Operation(summary = "Listar veículos do cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de veículos retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @GetMapping("/{id}/veiculos")
    public List<VeiculoDto> listarVeiculos(
            @Parameter(description = "ID do cliente") @PathVariable Long id) {
        return vinculos.listarVeiculosDoCliente(id).stream().map(VeiculoPresenter::toDto).toList();
    }
}
