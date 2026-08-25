package com.fiap.mecanica.estoque.adapter.in.web.presenter;

import com.fiap.mecanica.estoque.adapter.in.web.request.AtualizarInsumoRequest;
import com.fiap.mecanica.estoque.adapter.in.web.request.CadastrarEstoqueRequest;
import com.fiap.mecanica.estoque.adapter.in.web.request.DeduzirEstoqueItemRequest;
import com.fiap.mecanica.estoque.adapter.in.web.response.EstoqueDto;
import com.fiap.mecanica.estoque.adapter.in.web.response.InsumoDto;
import com.fiap.mecanica.estoque.application.command.AtualizarInsumoCommand;
import com.fiap.mecanica.estoque.application.command.DeduzirEstoqueItemCommand;
import com.fiap.mecanica.estoque.domain.Estoque;
import com.fiap.mecanica.estoque.domain.Insumo;

import java.util.List;

public class EstoquePresenter {

    private EstoquePresenter() {
        /* This utility class should not be instantiated */
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
        return AtualizarInsumoCommand.builder()
                .nome(request.nome())
                .precoUnitario(request.precoUnitario())
                .build();
    }

    public static List<DeduzirEstoqueItemCommand> toCommands(List<DeduzirEstoqueItemRequest> itens) {
        return itens.stream()
                .map(item -> DeduzirEstoqueItemCommand.builder()
                        .insumoId(item.insumoId())
                        .quantidade(item.quantidade())
                        .build())
                .toList();
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
