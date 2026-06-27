package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.ClienteVeiculo;
import com.fiap.mecanica.domain.Veiculo;
import com.fiap.mecanica.dto.VeiculoDto;
import com.fiap.mecanica.exception.ClienteNotFound;
import com.fiap.mecanica.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.exception.VinculoJaExistente;
import com.fiap.mecanica.repository.ClienteRepository;
import com.fiap.mecanica.repository.ClienteVeiculoRepository;
import com.fiap.mecanica.repository.VeiculoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class VinculoVeiculoService {

    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final ClienteVeiculoRepository clienteVeiculoRepository;

    public List<VeiculoDto> listarVeiculosDoCliente(Long clienteId) {
        clienteRepository.findById(clienteId).orElseThrow(() -> new ClienteNotFound(clienteId));
        return clienteVeiculoRepository.findByClienteId(clienteId)
                .stream()
                .map(cv -> VeiculoDto.builder()
                        .id(cv.getVeiculo().getId())
                        .marca(cv.getVeiculo().getMarca())
                        .modelo(cv.getVeiculo().getModelo())
                        .placa(cv.getVeiculo().getPlaca())
                        .ano(cv.getVeiculo().getAno())
                        .build())
                .toList();
    }

    public void vincularVeiculo(Long clienteId, Long veiculoId) {
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() -> new ClienteNotFound(clienteId));
        Veiculo veiculo = veiculoRepository.findById(veiculoId).orElseThrow(() -> new VeiculoNaoEncontradoException(veiculoId));

        if(clienteVeiculoRepository.existsByClienteIdAndVeiculoId(clienteId, veiculoId)) {
            throw new VinculoJaExistente(clienteId, veiculoId);
        }

        ClienteVeiculo vinculo = ClienteVeiculo.builder()
                .cliente(cliente)
                .veiculo(veiculo)
                .build();

        clienteVeiculoRepository.save(vinculo);
    }
}
