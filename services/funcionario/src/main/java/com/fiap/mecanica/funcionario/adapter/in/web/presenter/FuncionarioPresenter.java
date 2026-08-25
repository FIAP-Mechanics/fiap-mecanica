package com.fiap.mecanica.funcionario.adapter.in.web.presenter;

import com.fiap.mecanica.funcionario.adapter.in.web.request.AtualizarFuncionarioRequest;
import com.fiap.mecanica.funcionario.adapter.in.web.request.CadastrarFuncionarioRequest;
import com.fiap.mecanica.funcionario.adapter.in.web.response.FuncionarioDto;
import com.fiap.mecanica.funcionario.application.command.AtualizarFuncionarioCommand;
import com.fiap.mecanica.funcionario.domain.Funcionario;

public class FuncionarioPresenter {

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
        return AtualizarFuncionarioCommand.builder()
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
