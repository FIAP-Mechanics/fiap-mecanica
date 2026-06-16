package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.dto.ClienteDto;
import com.fiap.mecanica.exception.ClienteExistente;
import com.fiap.mecanica.exception.ClienteNotFound;
import com.fiap.mecanica.repository.ClienteRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@AllArgsConstructor
@Service
public class ClienteService {

    private final ClienteRepository repository;

    public List<Cliente> buscarClientes(){
        return repository.findAll();
    }

    public Cliente buscarClientePorId(Long id){
        return repository.findById(id).orElseThrow(() -> new ClienteNotFound(id));
    }

    public Cliente cadastrarCliente(Cliente cliente) {
        boolean clienteEncontrado = repository.existsByDocumento(cliente.getDocumento());
        if(clienteEncontrado) {
            throw new ClienteExistente(cliente.getDocumento());
        }
        return repository.save(cliente);
    }

    public Cliente atualizarCliente(Long id, ClienteDto clienteDto){
        Cliente cliente = repository.findById(id).orElseThrow(() -> new ClienteNotFound(id));
        atualizarCampo(clienteDto.nome(), cliente.getNome(), cliente::setNome);
        atualizarCampo(clienteDto.documento(), cliente.getDocumento(), cliente::setDocumento);
        atualizarCampo(clienteDto.email(), cliente.getEmail(), cliente::setEmail);
        atualizarCampo(clienteDto.telefone(), cliente.getTelefone(), cliente::setTelefone);
        atualizarCampo(clienteDto.endereco(), cliente.getEndereco(), cliente::setEndereco);

        return repository.save(cliente);
    }

    private <T> void atualizarCampo(T valorNovo, T valorAntigo, Consumer<T> setter){
        if(valorNovo != null && !Objects.equals(valorNovo, valorAntigo)){
            setter.accept(valorNovo);
        }
    }
}
