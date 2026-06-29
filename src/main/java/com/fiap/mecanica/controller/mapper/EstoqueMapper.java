package com.fiap.mecanica.controller.mapper;

import com.fiap.mecanica.controller.request.AtualizarInsumoRequest;
import com.fiap.mecanica.controller.request.CadastrarEstoqueRequest;
import com.fiap.mecanica.domain.Estoque;
import com.fiap.mecanica.domain.Insumo;
import com.fiap.mecanica.dto.EstoqueDto;
import com.fiap.mecanica.dto.InsumoDto;

public class EstoqueMapper {
    private EstoqueMapper() {
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

    public static InsumoDto toDto(AtualizarInsumoRequest request) {
        return InsumoDto.builder()
                .nome(request.nome())
                .precoUnitario(request.precoUnitario())
                .build();
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
