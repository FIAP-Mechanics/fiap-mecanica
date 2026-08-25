package com.fiap.mecanica.funcionario.application.usecase;

import com.fiap.mecanica.funcionario.application.command.AtualizarFuncionarioCommand;
import com.fiap.mecanica.funcionario.application.port.in.FuncionarioUseCase;
import com.fiap.mecanica.funcionario.application.port.out.FuncionarioGateway;
import com.fiap.mecanica.funcionario.application.port.out.PasswordEncoderGateway;
import com.fiap.mecanica.funcionario.domain.Funcionario;
import com.fiap.mecanica.funcionario.exception.ConflitoException;
import com.fiap.mecanica.funcionario.exception.FuncionarioInativoException;
import com.fiap.mecanica.funcionario.exception.FuncionarioJaAtivoException;
import com.fiap.mecanica.funcionario.exception.FuncionarioNotFound;

import java.util.List;
import java.util.function.Consumer;

public class FuncionarioInteractor implements FuncionarioUseCase {

    private final FuncionarioGateway funcionarioGateway;
    private final PasswordEncoderGateway passwordEncoderGateway;

    public FuncionarioInteractor(FuncionarioGateway funcionarioGateway, PasswordEncoderGateway passwordEncoderGateway) {
        this.funcionarioGateway = funcionarioGateway;
        this.passwordEncoderGateway = passwordEncoderGateway;
    }

    @Override
    public List<Funcionario> buscarTodos() {
        return funcionarioGateway.buscarTodos();
    }

    @Override
    public Funcionario cadastrarFuncionario(Funcionario funcionario) {
        if (funcionarioGateway.existePorEmail(funcionario.getEmail())) {
            throw new ConflitoException("Já existe um funcionário cadastrado com o e-mail: " + funcionario.getEmail()) {
            };
        }
        funcionario.setSenha(passwordEncoderGateway.encode(funcionario.getSenha()));
        return funcionarioGateway.salvar(funcionario);
    }

    @Override
    public Funcionario buscarFuncionarioPorId(Long id) {
        Funcionario funcionario = funcionarioGateway.buscarPorId(id).orElseThrow(() -> new FuncionarioNotFound(id));
        if (!funcionario.isAtivo()) throw new FuncionarioInativoException(id);
        return funcionario;
    }

    @Override
    public Funcionario buscarPorEmail(String email) {
        Funcionario funcionario = funcionarioGateway.buscarPorEmail(email).orElseThrow(() -> new FuncionarioNotFound(email));
        if (!funcionario.isAtivo()) throw new FuncionarioInativoException(funcionario.getId());
        return funcionario;
    }

    @Override
    public Funcionario atualizarFuncionario(Long id, AtualizarFuncionarioCommand command) {
        Funcionario funcionario = this.buscarFuncionarioPorId(id);

        atualizaSeExistente(command.email(), funcionario::setEmail);
        atualizaSeExistente(command.senha(), senha -> funcionario.setSenha(passwordEncoderGateway.encode(senha)));
        atualizaSeExistente(command.nome(), funcionario::setNome);
        atualizaSeExistente(command.funcao(), funcionario::setFuncao);

        return funcionarioGateway.salvar(funcionario);
    }

    @Override
    public Funcionario excluirFuncionario(Long id) {
        Funcionario funcionario = this.buscarFuncionarioPorId(id);
        funcionario.setAtivo(false);
        return funcionarioGateway.salvar(funcionario);
    }

    @Override
    public Funcionario reativarFuncionario(Long id) {
        Funcionario funcionario = funcionarioGateway.buscarPorId(id).orElseThrow(() -> new FuncionarioNotFound(id));
        if (funcionario.isAtivo()) throw new FuncionarioJaAtivoException(id);
        funcionario.setAtivo(true);
        return funcionarioGateway.salvar(funcionario);
    }

    private <T> void atualizaSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) {
            setter.accept(valor);
        }
    }
}
