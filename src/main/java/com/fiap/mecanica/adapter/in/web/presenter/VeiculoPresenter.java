package com.fiap.mecanica.adapter.in.web.presenter;

import com.fiap.mecanica.adapter.in.web.request.AtualizarVeiculoRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarVeiculoRequest;
import com.fiap.mecanica.adapter.in.web.response.VeiculoDto;
import com.fiap.mecanica.application.command.AtualizarVeiculoCommand;
import com.fiap.mecanica.domain.Veiculo;

public final class VeiculoPresenter {

    private VeiculoPresenter() {
    }

    public static Veiculo toEntity(CadastrarVeiculoRequest request) {
        return Veiculo.builder()
                .marca(request.marca())
                .modelo(request.modelo())
                .placa(request.placa())
                .ano(request.ano())
                .build();
    }

    public static AtualizarVeiculoCommand toCommand(AtualizarVeiculoRequest request) {
        return new AtualizarVeiculoCommand(request.marca(), request.modelo(), request.placa(), request.ano());
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
