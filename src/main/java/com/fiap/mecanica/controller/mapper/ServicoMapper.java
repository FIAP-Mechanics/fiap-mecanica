package com.fiap.mecanica.controller.mapper;

import com.fiap.mecanica.controller.request.AtualizarServicoRequest;
import com.fiap.mecanica.controller.request.CadastrarServicoRequest;
import com.fiap.mecanica.domain.Servico;
import com.fiap.mecanica.dto.ServicoDto;

import java.util.ArrayList;

public class ServicoMapper {
    private ServicoMapper() {
        /* This utility class should not be instantiated */
    }

    public static Servico toEntity(CadastrarServicoRequest request) {
        Servico servico = Servico.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .valor(request.valor())
                .build();
        servico.atualizarInsumos(request.insumos() == null ? new ArrayList<>() : request.insumos());
        return servico;
    }

    public static ServicoDto toDto(AtualizarServicoRequest request) {
        return ServicoDto.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .valor(request.valor())
                .insumos(request.insumos())
                .build();
    }

    public static ServicoDto toDto(Servico servico) {
        return ServicoDto.builder()
                .id(servico.getId())
                .nome(servico.getNome())
                .descricao(servico.getDescricao())
                .valor(servico.getValor())
                .insumos(servico.getInsumos())
                .build();
    }
}
