package com.fiap.mecanica.veiculo.controller.mapper;

import com.fiap.mecanica.veiculo.controller.request.AtualizarVeiculoRequest;
import com.fiap.mecanica.veiculo.controller.request.CadastrarVeiculoRequest;
import com.fiap.mecanica.veiculo.domain.Veiculo;
import com.fiap.mecanica.veiculo.dto.VeiculoDto;

public class VeiculoMapper {
    private VeiculoMapper() {
    }

    public static Veiculo toEntity(CadastrarVeiculoRequest request) {
        return Veiculo.builder()
                .marca(request.marca())
                .modelo(request.modelo())
                .placa(request.placa())
                .ano(request.ano())
                .build();
    }

    public static VeiculoDto toDto(AtualizarVeiculoRequest request) {
        return VeiculoDto.builder()
                .marca(request.marca())
                .modelo(request.modelo())
                .placa(request.placa())
                .ano(request.ano())
                .build();
    }

    public static VeiculoDto toDto(Veiculo veiculo) {
        return VeiculoDto.builder()
                .id(veiculo.getId())
                .marca(veiculo.getMarca())
                .modelo(veiculo.getModelo())
                .placa(veiculo.getPlaca())
                .ano(veiculo.getAno())
                .build();
    }
}
