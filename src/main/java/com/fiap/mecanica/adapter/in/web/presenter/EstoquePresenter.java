package com.fiap.mecanica.adapter.in.web.presenter;

import com.fiap.mecanica.adapter.in.web.request.AtualizarInsumoRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarEstoqueRequest;
import com.fiap.mecanica.adapter.in.web.response.EstoqueDto;
import com.fiap.mecanica.adapter.in.web.response.InsumoDto;
import com.fiap.mecanica.application.command.AtualizarInsumoCommand;
import com.fiap.mecanica.domain.Estoque;
import com.fiap.mecanica.domain.Insumo;

public final class EstoquePresenter {

    private EstoquePresenter() {
    }

    public static Estoque toEntity(CadastrarEstoqueRequest request) {
        Insumo insumo = Insumo.builder()
                .nome(request.insumo().nome())
                .precoUnitario(request.insumo().precoUnitario())
                .build();
        return Estoque.builder()
                .insumo(insumo)
                .quantidadeInsumo(request.quantidade())
                .build();
    }

    public static AtualizarInsumoCommand toCommand(AtualizarInsumoRequest request) {
        return new AtualizarInsumoCommand(request.nome(), request.precoUnitario());
    }

    public static EstoqueDto toDto(Estoque estoque) {
        Insumo insumo = estoque.getInsumo();
        return EstoqueDto.builder()
                .insumo(InsumoDto.builder()
                        .id(insumo.getId())
                        .nome(insumo.getNome())
                        .precoUnitario(insumo.getPrecoUnitario())
                        .build())
                .quantidadeInsumo(estoque.getQuantidadeInsumo())
                .build();
    }
}
