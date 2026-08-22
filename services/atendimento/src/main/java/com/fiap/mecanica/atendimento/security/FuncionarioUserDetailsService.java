package com.fiap.mecanica.atendimento.security;

import com.fiap.mecanica.atendimento.domain.Funcionario;
import com.fiap.mecanica.atendimento.repository.FuncionarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class FuncionarioUserDetailsService implements UserDetailsService {

    private final FuncionarioRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        Funcionario funcionario = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Funcionario nao encontrado: " + email));

        return User.withUsername(funcionario.getEmail())
                .password(funcionario.getSenha())
                .authorities("ROLE_" + funcionario.getFuncao().name())
                .disabled(!funcionario.isAtivo())
                .build();
    }
}
