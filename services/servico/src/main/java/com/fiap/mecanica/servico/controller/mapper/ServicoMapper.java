package com.fiap.mecanica.servico.controller.mapper;

import com.fiap.mecanica.servico.controller.request.AtualizarServicoRequest;
import com.fiap.mecanica.servico.controller.request.CadastrarServicoRequest;
import com.fiap.mecanica.servico.domain.Servico;
import com.fiap.mecanica.servico.dto.ServicoDto;

public class ServicoMapper {
    private ServicoMapper() {
    }

    public static Servico toEntity(CadastrarServicoRequest request) {
        return Servico.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .valor(request.valor())
                .build();
    }

    public static ServicoDto toDto(AtualizarServicoRequest request) {
        return ServicoDto.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .valor(request.valor())
                .build();
    }

    public static ServicoDto toDto(Servico servico) {
        return ServicoDto.builder()
                .id(servico.getId())
                .nome(servico.getNome())
                .descricao(servico.getDescricao())
                .valor(servico.getValor())
                .build();
    }
}
