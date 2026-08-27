package com.fiap.mecanica.funcionario.infrastructure.configuration;

import com.fiap.mecanica.funcionario.application.port.in.FuncionarioUseCase;
import com.fiap.mecanica.funcionario.application.port.out.FuncionarioGateway;
import com.fiap.mecanica.funcionario.application.port.out.PasswordEncoderGateway;
import com.fiap.mecanica.funcionario.application.usecase.FuncionarioInteractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public FuncionarioUseCase funcionarioUseCase(FuncionarioGateway funcionarioGateway, PasswordEncoderGateway passwordEncoderGateway) {
        return new FuncionarioInteractor(funcionarioGateway, passwordEncoderGateway);
    }
}
