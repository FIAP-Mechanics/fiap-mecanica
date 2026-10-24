package com.fiap.mecanica.funcionario.infrastructure.configuration;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
public class ObservabilidadeConfiguration {

    private static final String PREFIXO_ACTUATOR = "/actuator";

    @Bean
    public ObservationPredicate ignorarRequisicoesDoActuator() {
        return (nome, contexto) -> !(contexto instanceof ServerRequestObservationContext servidor
                && servidor.getCarrier().getRequestURI().startsWith(PREFIXO_ACTUATOR));
    }
}
