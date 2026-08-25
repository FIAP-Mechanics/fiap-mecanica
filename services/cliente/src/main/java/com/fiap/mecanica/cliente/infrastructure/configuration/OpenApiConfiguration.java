package com.fiap.mecanica.cliente.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FIAP Mecânica - Microsserviço de Clientes")
                        .version("1.0.0")
                        .description("API REST de gestão de clientes da oficina mecânica.")
                        .contact(new Contact()
                                .name("Equipe FIAP Mecânica")
                                .email("contato@mecanica.fiap.com.br"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
