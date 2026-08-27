package com.fiap.mecanica.servico.infrastructure.configuration;

import com.fiap.mecanica.servico.application.port.in.ServicoUseCase;
import com.fiap.mecanica.servico.application.port.out.ServicoGateway;
import com.fiap.mecanica.servico.application.usecase.ServicoInteractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public ServicoUseCase servicoUseCase(ServicoGateway servicoGateway) {
        return new ServicoInteractor(servicoGateway);
    }
}
