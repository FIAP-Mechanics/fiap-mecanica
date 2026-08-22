package com.fiap.mecanica.adapter.out.security;

import com.fiap.mecanica.application.port.out.FuncionarioGateway;
import com.fiap.mecanica.domain.Funcionario;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class FuncionarioUserDetailsAdapter implements UserDetailsService {

    private final FuncionarioGateway funcionarioGateway;

    public FuncionarioUserDetailsAdapter(FuncionarioGateway funcionarioGateway) {
        this.funcionarioGateway = funcionarioGateway;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Funcionario funcionario = funcionarioGateway.buscarPorEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Funcionario nao encontrado: " + email));

        return User.withUsername(funcionario.getEmail())
                .password(funcionario.getSenha())
                .authorities("ROLE_" + funcionario.getFuncao().name())
                .disabled(!funcionario.isAtivo())
                .build();
    }
}
