package com.fiap.mecanica.atendimento.client;

import com.fiap.mecanica.atendimento.client.dto.ClienteIntegracaoDto;
import com.fiap.mecanica.atendimento.config.InternalServiceTokenProvider;
import com.fiap.mecanica.atendimento.exception.ClienteNaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class ClienteClient {

    private final RestClient restClient;
    private final InternalServiceTokenProvider internalServiceTokenProvider;

    public ClienteClient(
            @Value("${clients.cliente-service.base-url}") String baseUrl,
            InternalServiceTokenProvider internalServiceTokenProvider) {
        this.restClient = RestClient.create(baseUrl);
        this.internalServiceTokenProvider = internalServiceTokenProvider;
    }

    public ClienteIntegracaoDto buscarCliente(Long id) {
        try {
            return restClient.get()
                    .uri("/cliente/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, internalServiceTokenProvider.obterAuthorizationHeader())
                    .retrieve()
                    .body(ClienteIntegracaoDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ClienteNaoEncontradoException(id);
        }
    }
}
