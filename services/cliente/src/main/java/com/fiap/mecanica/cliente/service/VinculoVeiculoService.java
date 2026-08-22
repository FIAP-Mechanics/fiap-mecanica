package com.fiap.mecanica.cliente.service;

import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.domain.ClienteVeiculo;
import com.fiap.mecanica.cliente.dto.VeiculoDto;
import com.fiap.mecanica.cliente.exception.ClienteNotFound;
import com.fiap.mecanica.cliente.exception.VinculoJaExistente;
import com.fiap.mecanica.cliente.repository.ClienteRepository;
import com.fiap.mecanica.cliente.repository.ClienteVeiculoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class VinculoVeiculoService {

    private final ClienteRepository clienteRepository;
    private final ClienteVeiculoRepository clienteVeiculoRepository;

    public List<VeiculoDto> listarVeiculosDoCliente(Long clienteId) {
        clienteRepository.findById(clienteId).orElseThrow(() -> new ClienteNotFound(clienteId));
        return clienteVeiculoRepository.findByClienteId(clienteId)
                .stream()
                .map(cv -> VeiculoDto.builder()
                        .id(cv.getVeiculoId())
                        .marca(cv.getMarca())
                        .modelo(cv.getModelo())
                        .placa(cv.getPlaca())
                        .ano(cv.getAno())
                        .build())
                .toList();
    }

    public void vincularVeiculo(Long clienteId, Long veiculoId, String placa, String marca, String modelo, Integer ano) {
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() -> new ClienteNotFound(clienteId));

        if (clienteVeiculoRepository.existsByClienteIdAndVeiculoId(clienteId, veiculoId)) {
            throw new VinculoJaExistente(clienteId, veiculoId);
        }

        ClienteVeiculo vinculo = ClienteVeiculo.builder()
                .cliente(cliente)
                .veiculoId(veiculoId)
                .placa(placa)
                .marca(marca)
                .modelo(modelo)
                .ano(ano)
                .build();

        clienteVeiculoRepository.save(vinculo);
    }
}
