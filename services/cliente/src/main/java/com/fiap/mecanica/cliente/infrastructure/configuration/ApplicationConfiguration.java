package com.fiap.mecanica.cliente.infrastructure.configuration;

import com.fiap.mecanica.cliente.application.port.in.ClienteUseCase;
import com.fiap.mecanica.cliente.application.port.in.VinculoVeiculoUseCase;
import com.fiap.mecanica.cliente.application.port.out.ClienteGateway;
import com.fiap.mecanica.cliente.application.port.out.ClienteVeiculoGateway;
import com.fiap.mecanica.cliente.application.usecase.ClienteInteractor;
import com.fiap.mecanica.cliente.application.usecase.VinculoVeiculoInteractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public ClienteUseCase clienteUseCase(ClienteGateway clienteGateway) {
        return new ClienteInteractor(clienteGateway);
    }

    @Bean
    public VinculoVeiculoUseCase vinculoVeiculoUseCase(ClienteGateway clienteGateway, ClienteVeiculoGateway clienteVeiculoGateway) {
        return new VinculoVeiculoInteractor(clienteGateway, clienteVeiculoGateway);
    }
}
