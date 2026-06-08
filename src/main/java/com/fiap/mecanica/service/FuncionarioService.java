package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Funcionario;
import com.fiap.mecanica.dto.FuncionarioDto;
import com.fiap.mecanica.exception.FuncionarioInativoException;
import com.fiap.mecanica.exception.FuncionarioJaAtivoException;
import com.fiap.mecanica.exception.FuncionarioNotFound;
import com.fiap.mecanica.repository.FuncionarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@AllArgsConstructor
@Service
public class FuncionarioService {
    private final FuncionarioRepository repository;

    public Funcionario cadastrarFuncionario(Funcionario funcionario) {
        return repository.save(funcionario);
    }

    public Funcionario buscarFuncionarioPorId(Long id) {
        Funcionario funcionario = repository.findById(id).orElseThrow(() -> new FuncionarioNotFound(id));
        if (!funcionario.isAtivo()) throw new FuncionarioInativoException(id);
        return funcionario;
    }

    public Funcionario atualizarFuncionario(Long id, FuncionarioDto funcionarioDto) {
        Funcionario funcionario = this.buscarFuncionarioPorId(id);

        atualizaSeExistente(funcionarioDto.email(), funcionario::setEmail);
        atualizaSeExistente(funcionarioDto.senha(), funcionario::setSenha);
        atualizaSeExistente(funcionarioDto.nome(), funcionario::setNome);
        atualizaSeExistente(funcionarioDto.funcao(), funcionario::setFuncao);

        return repository.save(funcionario);
    }

    public Funcionario excluirFuncionario(Long id) {
        Funcionario funcionario = this.buscarFuncionarioPorId(id);
        funcionario.setAtivo(false);
        return repository.save(funcionario);
    }

    public Funcionario reativarFuncionario(Long id) {
        Funcionario funcionario = repository.findById(id).orElseThrow(() -> new FuncionarioNotFound(id));
        if (funcionario.isAtivo()) throw new FuncionarioJaAtivoException(id);
        funcionario.setAtivo(true);
        return repository.save(funcionario);
    }

    private <T> void atualizaSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) {
            setter.accept(valor);
        }
    }
}
