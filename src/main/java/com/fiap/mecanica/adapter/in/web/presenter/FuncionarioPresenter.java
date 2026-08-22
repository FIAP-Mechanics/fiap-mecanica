package com.fiap.mecanica.adapter.in.web.presenter;

import com.fiap.mecanica.adapter.in.web.request.AtualizarFuncionarioRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarFuncionarioRequest;
import com.fiap.mecanica.adapter.in.web.response.FuncionarioDto;
import com.fiap.mecanica.application.command.AtualizarFuncionarioCommand;
import com.fiap.mecanica.domain.Funcionario;

public final class FuncionarioPresenter {

    private FuncionarioPresenter() {
    }

    public static Funcionario toEntity(CadastrarFuncionarioRequest request) {
        return Funcionario.builder()
                .email(request.email())
                .senha(request.senha())
                .nome(request.nome())
                .funcao(request.funcao())
                .build();
    }

    public static AtualizarFuncionarioCommand toCommand(AtualizarFuncionarioRequest request) {
        return new AtualizarFuncionarioCommand(request.email(), request.senha(), request.nome(), request.funcao());
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
