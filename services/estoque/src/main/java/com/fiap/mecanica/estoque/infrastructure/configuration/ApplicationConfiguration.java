package com.fiap.mecanica.estoque.infrastructure.configuration;

import com.fiap.mecanica.estoque.application.port.in.EstoqueUseCase;
import com.fiap.mecanica.estoque.application.port.out.EstoqueGateway;
import com.fiap.mecanica.estoque.application.usecase.EstoqueInteractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public EstoqueUseCase estoqueUseCase(EstoqueGateway estoqueGateway) {
        return new EstoqueInteractor(estoqueGateway);
    }
}
