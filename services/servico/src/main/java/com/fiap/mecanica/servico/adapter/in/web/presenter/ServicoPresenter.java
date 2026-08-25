package com.fiap.mecanica.servico.adapter.in.web.presenter;

import com.fiap.mecanica.servico.adapter.in.web.request.AtualizarServicoRequest;
import com.fiap.mecanica.servico.adapter.in.web.request.CadastrarServicoRequest;
import com.fiap.mecanica.servico.adapter.in.web.response.ServicoDto;
import com.fiap.mecanica.servico.application.command.AtualizarServicoCommand;
import com.fiap.mecanica.servico.domain.Servico;

public class ServicoPresenter {

    private ServicoPresenter() {
    }

    public static Servico toEntity(CadastrarServicoRequest request) {
        return Servico.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .valor(request.valor())
                .build();
    }

    public static AtualizarServicoCommand toCommand(AtualizarServicoRequest request) {
        return AtualizarServicoCommand.builder()
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
