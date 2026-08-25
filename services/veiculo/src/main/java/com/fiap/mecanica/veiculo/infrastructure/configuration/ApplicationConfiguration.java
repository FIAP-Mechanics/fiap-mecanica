package com.fiap.mecanica.veiculo.infrastructure.configuration;

import com.fiap.mecanica.veiculo.application.port.in.VeiculoUseCase;
import com.fiap.mecanica.veiculo.application.port.out.VeiculoGateway;
import com.fiap.mecanica.veiculo.application.usecase.VeiculoInteractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public VeiculoUseCase veiculoUseCase(VeiculoGateway veiculoGateway) {
        return new VeiculoInteractor(veiculoGateway);
    }
}
