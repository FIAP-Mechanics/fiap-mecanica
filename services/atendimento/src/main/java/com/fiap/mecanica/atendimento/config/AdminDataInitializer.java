package com.fiap.mecanica.atendimento.config;

import com.fiap.mecanica.atendimento.domain.Funcao;
import com.fiap.mecanica.atendimento.domain.Funcionario;
import com.fiap.mecanica.atendimento.repository.FuncionarioRepository;
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

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.nome}")
    private String adminNome;

    @Override
    public void run(String @NonNull ... args) {
        if (funcionarioRepository.findByEmail(adminEmail).isEmpty()) {
            log.info("Criando usuário administrador padrão: {}", adminEmail);

            Funcionario admin = Funcionario.builder()
                    .nome(adminNome)
                    .email(adminEmail)
                    .senha(passwordEncoder.encode(adminPassword))
                    .funcao(Funcao.ADMIN)
                    .ativo(true)
                    .build();

            funcionarioRepository.save(admin);
            log.info("Usuário administrador padrão criado com sucesso.");
        } else {
            log.info("Usuário administrador padrão já existe.");
        }
    }
}
