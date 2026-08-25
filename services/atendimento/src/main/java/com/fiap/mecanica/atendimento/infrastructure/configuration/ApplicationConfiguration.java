package com.fiap.mecanica.atendimento.infrastructure.configuration;

import com.fiap.mecanica.atendimento.application.port.in.AtendimentoUseCase;
import com.fiap.mecanica.atendimento.application.port.in.AuthUseCase;
import com.fiap.mecanica.atendimento.application.port.in.TemplateUseCase;
import com.fiap.mecanica.atendimento.application.port.out.AuthenticationGateway;
import com.fiap.mecanica.atendimento.application.port.out.ClienteIntegracaoGateway;
import com.fiap.mecanica.atendimento.application.port.out.EstoqueIntegracaoGateway;
import com.fiap.mecanica.atendimento.application.port.out.NotificationGateway;
import com.fiap.mecanica.atendimento.application.port.out.OrdemServicoGateway;
import com.fiap.mecanica.atendimento.application.port.out.ServicoIntegracaoGateway;
import com.fiap.mecanica.atendimento.application.port.out.TemplateGateway;
import com.fiap.mecanica.atendimento.application.port.out.TokenGateway;
import com.fiap.mecanica.atendimento.application.port.out.VeiculoIntegracaoGateway;
import com.fiap.mecanica.atendimento.application.usecase.AtendimentoInteractor;
import com.fiap.mecanica.atendimento.application.usecase.AuthInteractor;
import com.fiap.mecanica.atendimento.application.usecase.TemplateInteractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public AtendimentoUseCase atendimentoUseCase(OrdemServicoGateway ordemServicoGateway,
                                                  ClienteIntegracaoGateway clienteIntegracaoGateway,
                                                  VeiculoIntegracaoGateway veiculoIntegracaoGateway,
                                                  ServicoIntegracaoGateway servicoIntegracaoGateway,
                                                  EstoqueIntegracaoGateway estoqueIntegracaoGateway,
                                                  NotificationGateway notificationGateway) {
        return new AtendimentoInteractor(ordemServicoGateway, clienteIntegracaoGateway, veiculoIntegracaoGateway,
                servicoIntegracaoGateway, estoqueIntegracaoGateway, notificationGateway);
    }

    @Bean
    public AuthUseCase authUseCase(AuthenticationGateway authenticationGateway, TokenGateway tokenGateway) {
        return new AuthInteractor(authenticationGateway, tokenGateway);
    }

    @Bean
    public TemplateUseCase templateUseCase(TemplateGateway templateGateway) {
        return new TemplateInteractor(templateGateway);
    }
}
