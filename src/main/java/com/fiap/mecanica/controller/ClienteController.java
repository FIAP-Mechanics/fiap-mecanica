package com.fiap.mecanica.controller;

import com.fiap.mecanica.controller.mapper.ClienteMapper;
import com.fiap.mecanica.controller.request.AtualizarClienteRequest;
import com.fiap.mecanica.controller.request.CadastrarClienteRequest;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.dto.ClienteDto;
import com.fiap.mecanica.service.ClienteService;
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

import static com.fiap.mecanica.controller.mapper.ClienteMapper.toDto;
import static com.fiap.mecanica.controller.mapper.ClienteMapper.toEntity;

@AllArgsConstructor
@RestController
@RequestMapping("/cliente")
@Tag(name = "Clientes", description = "Operações de gerenciamento de clientes")
public class ClienteController {

    private final ClienteService service;

    @Operation(summary = "Buscar lista de clientes", description = "Retorna lista de clientes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "404", description = "Clinete não encontrado", content = @Content)
    })
    @GetMapping
    public List<ClienteDto> getList() {
        List<Cliente> clientes = service.buscarClientes();
        return clientes.stream()
                .map(ClienteMapper::toDto)
                .toList();
    }

    @Operation(summary = "Buscar cliente por CPF/CNPJ", description = "Identifica o cliente pelo documento informado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "404", description = "Cliente nÃ£o encontrado", content = @Content)
    })
    @GetMapping("/documento")
    public ClienteDto getByDocumento(
            @Parameter(description = "CPF ou CNPJ do cliente") @RequestParam String documento
    ) {
        return toDto(service.buscarClientePorDocumento(documento));
    }

    @Operation(summary = "Buscar cliente por ID", description = "Retorna os dados de um cliente pelo seu identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "404", description = "Clinete não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ClienteDto get(
            @Parameter(description = "ID do cliente") @PathVariable Long id
    ) {
        return toDto(service.buscarClientePorId(id));
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
        return toDto(service.cadastrarCliente(cliente));
    }

    @Operation(summary = "Atualizar cliente", description = "Atualiza todos os dados de um cliente existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteDto.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ClienteDto update(
            @Parameter(description = "ID do cliente") @PathVariable Long id,
            @RequestBody AtualizarClienteRequest request){
        ClienteDto cliente = toDto(request);
        return toDto(service.atualizarCliente(id, cliente));
    }
}
