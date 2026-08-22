package com.fiap.mecanica.adapter.in.web.presenter;

import com.fiap.mecanica.adapter.in.web.request.AtualizarServicoRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarServicoRequest;
import com.fiap.mecanica.adapter.in.web.response.ServicoDto;
import com.fiap.mecanica.application.command.AtualizarServicoCommand;
import com.fiap.mecanica.domain.Servico;

public final class ServicoPresenter {

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
        return new AtualizarServicoCommand(request.nome(), request.descricao(), request.valor());
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
