package com.fiap.mecanica.funcionario.service;

import com.fiap.mecanica.funcionario.domain.Funcionario;
import com.fiap.mecanica.funcionario.dto.FuncionarioDto;
import com.fiap.mecanica.funcionario.exception.ConflitoException;
import com.fiap.mecanica.funcionario.exception.FuncionarioInativoException;
import com.fiap.mecanica.funcionario.exception.FuncionarioJaAtivoException;
import com.fiap.mecanica.funcionario.exception.FuncionarioNotFound;
import com.fiap.mecanica.funcionario.repository.FuncionarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
@Service
public class FuncionarioService {
    private final FuncionarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public List<Funcionario> buscarTodos() {
        return repository.findAll();
    }

    public Funcionario cadastrarFuncionario(Funcionario funcionario) {
        if (repository.existsByEmail(funcionario.getEmail())) {
            throw new ConflitoException("Já existe um funcionário cadastrado com o e-mail: " + funcionario.getEmail()) {
            };
        }
        funcionario.setSenha(passwordEncoder.encode(funcionario.getSenha()));
        return repository.save(funcionario);
    }

    public Funcionario buscarFuncionarioPorId(Long id) {
        Funcionario funcionario = repository.findById(id).orElseThrow(() -> new FuncionarioNotFound(id));
        if (!funcionario.isAtivo()) throw new FuncionarioInativoException(id);
        return funcionario;
    }

    public Funcionario buscarPorEmail(String email) {
        Funcionario funcionario = repository.findByEmail(email).orElseThrow(() -> new FuncionarioNotFound(email));
        if (!funcionario.isAtivo()) throw new FuncionarioInativoException(funcionario.getId());
        return funcionario;
    }

    public Funcionario atualizarFuncionario(Long id, FuncionarioDto funcionarioDto) {
        Funcionario funcionario = this.buscarFuncionarioPorId(id);

        atualizaSeExistente(funcionarioDto.email(), funcionario::setEmail);
        atualizaSeExistente(funcionarioDto.senha(), senha -> funcionario.setSenha(passwordEncoder.encode(senha)));
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
