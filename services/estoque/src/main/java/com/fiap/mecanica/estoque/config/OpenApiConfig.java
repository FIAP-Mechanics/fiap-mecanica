package com.fiap.mecanica.estoque.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FIAP Mecânica - Microsserviço de Estoque e Insumos")
                        .version("1.0.0")
                        .description("API REST de gerenciamento de estoque, peças e insumos da oficina mecânica.")
                        .contact(new Contact()
                                .name("Equipe FIAP Mecânica")
                                .email("contato@mecanica.fiap.com.br"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
