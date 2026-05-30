package com.fiap.mecanica.controller.mapper;

import com.fiap.mecanica.controller.request.AtualizarFuncionarioRequest;
import com.fiap.mecanica.controller.request.CadastrarFuncionarioRequest;
import com.fiap.mecanica.domain.Funcionario;
import com.fiap.mecanica.dto.FuncionarioDto;

public class FuncionarioMapper {
    private FuncionarioMapper() {
        /* This utility class should not be instantiated */
    }

    public static Funcionario toEntity(CadastrarFuncionarioRequest request) {
        return Funcionario.builder()
                .email(request.email())
                .senha(request.senha())
                .nome(request.nome())
                .funcao(request.funcao())
                .build();
    }

    public static FuncionarioDto toDto(AtualizarFuncionarioRequest request) {
        return FuncionarioDto.builder()
                .email(request.email())
                .senha(request.senha())
                .nome(request.nome())
                .funcao(request.funcao())
                .build();
    }

    public static FuncionarioDto toDto(Funcionario funcionario) {
        return FuncionarioDto.builder()
                .id(funcionario.getId())
                .email(funcionario.getEmail())
                .nome(funcionario.getNome())
                .funcao(funcionario.getFuncao())
                .build();
    }
}
