package com.fiap.mecanica.cliente.service;

import com.fiap.mecanica.cliente.controller.mapper.ClienteMapper;
import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.dto.ClienteDto;
import com.fiap.mecanica.cliente.exception.ClienteExistente;
import com.fiap.mecanica.cliente.exception.ClienteNotFound;
import com.fiap.mecanica.cliente.repository.ClienteRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@AllArgsConstructor
@Service
public class ClienteService {

    private final ClienteRepository repository;

    public List<Cliente> buscarClientes() {
        return repository.findAll();
    }

    public Cliente buscarClientePorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new ClienteNotFound(id));
    }

    public Cliente buscarClientePorDocumento(String documento) {
        String documentoNormalizado = apenasDigitos(documento);
        return repository.findByDocumento(documentoNormalizado)
                .orElseThrow(() -> new ClienteNotFound(documentoNormalizado));
    }

    public Cliente cadastrarCliente(Cliente cliente) {
        String documentoNormalizado = apenasDigitos(cliente.getDocumento());
        cliente.setDocumento(documentoNormalizado);
        boolean clienteEncontrado = repository.existsByDocumento(documentoNormalizado);
        if (clienteEncontrado) {
            throw new ClienteExistente(cliente.getDocumento());
        }
        return repository.save(cliente);
    }

    public Cliente atualizarCliente(Long id, ClienteDto clienteDto) {
        Cliente cliente = repository.findById(id).orElseThrow(() -> new ClienteNotFound(id));
        atualizarCampo(clienteDto.nome(), cliente.getNome(), cliente::setNome);
        atualizarCampo(clienteDto.documento() != null ? apenasDigitos(clienteDto.documento()) : null, cliente.getDocumento(), cliente::setDocumento);
        atualizarCampo(clienteDto.email(), cliente.getEmail(), cliente::setEmail);
        atualizarCampo(clienteDto.telefone(), cliente.getTelefone(), cliente::setTelefone);
        atualizarCampo(ClienteMapper.toEntity(clienteDto.endereco()), cliente.getEndereco(), cliente::setEndereco);

        return repository.save(cliente);
    }

    private <T> void atualizarCampo(T valorNovo, T valorAntigo, Consumer<T> setter) {
        if (valorNovo != null && !Objects.equals(valorNovo, valorAntigo)) {
            setter.accept(valorNovo);
        }
    }

    private String apenasDigitos(String valor) {
        return valor != null ? valor.replaceAll("\\D", "") : "";
    }
}
