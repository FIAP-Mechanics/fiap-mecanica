package com.fiap.mecanica.atendimento.adapter.in.bootstrap;

import com.fiap.mecanica.atendimento.application.port.out.FuncionarioGateway;
import com.fiap.mecanica.atendimento.domain.Funcao;
import com.fiap.mecanica.atendimento.domain.Funcionario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminDataInitializer implements CommandLineRunner {

    private final FuncionarioGateway funcionarioGateway;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.nome}")
    private String adminNome;

    @Override
    public void run(String @NonNull ... args) {
        if (funcionarioGateway.buscarPorEmail(adminEmail).isEmpty()) {
            log.info("Criando usuário administrador padrão: {}", adminEmail);

            Funcionario admin = Funcionario.builder()
                    .nome(adminNome)
                    .email(adminEmail)
                    .senha(passwordEncoder.encode(adminPassword))
                    .funcao(Funcao.ADMIN)
                    .ativo(true)
                    .build();

            funcionarioGateway.salvar(admin);
            log.info("Usuário administrador padrão criado com sucesso.");
        } else {
            log.info("Usuário administrador padrão já existe.");
        }
    }
}
