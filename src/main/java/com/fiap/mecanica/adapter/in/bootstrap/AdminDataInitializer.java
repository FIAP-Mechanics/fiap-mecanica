package com.fiap.mecanica.adapter.in.bootstrap;

import com.fiap.mecanica.application.port.in.FuncionarioUseCase;
import com.fiap.mecanica.domain.Funcao;
import com.fiap.mecanica.domain.Funcionario;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AdminDataInitializer implements CommandLineRunner {

    private final FuncionarioUseCase funcionarioUseCase;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminNome;

    public AdminDataInitializer(
            FuncionarioUseCase funcionarioUseCase,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${app.admin.password}") String adminPassword,
            @Value("${app.admin.nome}") String adminNome) {
        this.funcionarioUseCase = funcionarioUseCase;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminNome = adminNome;
    }

    @Override
    public void run(String @NonNull ... args) {
        if (funcionarioUseCase.buscarFuncionarioPorEmail(adminEmail).isEmpty()) {
            log.info("Criando usuário administrador padrão: {}", adminEmail);

            Funcionario admin = Funcionario.builder()
                    .nome(adminNome)
                    .email(adminEmail)
                    .senha(adminPassword)
                    .funcao(Funcao.ADMIN)
                    .ativo(true)
                    .build();

            funcionarioUseCase.cadastrarFuncionario(admin);
            log.info("Usuário administrador padrão criado com sucesso.");
        } else {
            log.info("Usuário administrador padrão já existe.");
        }
    }
}
