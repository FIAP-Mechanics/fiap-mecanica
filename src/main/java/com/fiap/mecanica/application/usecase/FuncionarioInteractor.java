package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AtualizarFuncionarioCommand;
import com.fiap.mecanica.application.port.in.FuncionarioUseCase;
import com.fiap.mecanica.application.port.out.CodificadorSenhaGateway;
import com.fiap.mecanica.application.port.out.FuncionarioGateway;
import com.fiap.mecanica.domain.Funcionario;
import com.fiap.mecanica.exception.FuncionarioInativoException;
import com.fiap.mecanica.exception.FuncionarioJaAtivoException;
import com.fiap.mecanica.exception.FuncionarioNotFound;

import java.util.Optional;
import java.util.function.Consumer;

public class FuncionarioInteractor implements FuncionarioUseCase {

    private final FuncionarioGateway funcionarioGateway;
    private final CodificadorSenhaGateway codificadorSenhaGateway;

    public FuncionarioInteractor(
            FuncionarioGateway funcionarioGateway,
            CodificadorSenhaGateway codificadorSenhaGateway) {
        this.funcionarioGateway = funcionarioGateway;
        this.codificadorSenhaGateway = codificadorSenhaGateway;
    }

    @Override
    public Funcionario cadastrarFuncionario(Funcionario funcionario) {
        funcionario.setSenha(codificadorSenhaGateway.codificar(funcionario.getSenha()));
        return funcionarioGateway.salvar(funcionario);
    }

    @Override
    public Funcionario buscarFuncionarioPorId(Long id) {
        Funcionario funcionario = funcionarioGateway.buscarPorId(id)
                .orElseThrow(() -> new FuncionarioNotFound(id));
        if (!funcionario.isAtivo()) {
            throw new FuncionarioInativoException(id);
        }
        return funcionario;
    }

    @Override
    public Optional<Funcionario> buscarFuncionarioPorEmail(String email) {
        return funcionarioGateway.buscarPorEmail(email);
    }

    @Override
    public Funcionario atualizarFuncionario(Long id, AtualizarFuncionarioCommand command) {
        Funcionario funcionario = buscarFuncionarioPorId(id);
        atualizarSeExistente(command.email(), funcionario::setEmail);
        atualizarSeExistente(
                command.senha(),
                senha -> funcionario.setSenha(codificadorSenhaGateway.codificar(senha)));
        atualizarSeExistente(command.nome(), funcionario::setNome);
        atualizarSeExistente(command.funcao(), funcionario::setFuncao);
        return funcionarioGateway.salvar(funcionario);
    }

    @Override
    public Funcionario excluirFuncionario(Long id) {
        Funcionario funcionario = buscarFuncionarioPorId(id);
        funcionario.setAtivo(false);
        return funcionarioGateway.salvar(funcionario);
    }

    @Override
    public Funcionario reativarFuncionario(Long id) {
        Funcionario funcionario = funcionarioGateway.buscarPorId(id)
                .orElseThrow(() -> new FuncionarioNotFound(id));
        if (funcionario.isAtivo()) {
            throw new FuncionarioJaAtivoException(id);
        }
        funcionario.setAtivo(true);
        return funcionarioGateway.salvar(funcionario);
    }

    private <T> void atualizarSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) {
            setter.accept(valor);
        }
    }
}
