package com.fiap.mecanica.atendimento.adapter.out.client;

import com.fiap.mecanica.atendimento.adapter.out.client.dto.ClienteIntegracaoDto;
import com.fiap.mecanica.atendimento.application.port.out.ClienteIntegracaoGateway;
import com.fiap.mecanica.atendimento.exception.ClienteNaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class ClienteIntegracaoAdapter implements ClienteIntegracaoGateway {

    private final RestClient restClient;
    private final InternalServiceTokenProvider internalServiceTokenProvider;

    public ClienteIntegracaoAdapter(
            @Value("${clients.cliente-service.base-url}") String baseUrl,
            InternalServiceTokenProvider internalServiceTokenProvider) {
        this.restClient = RestClient.create(baseUrl);
        this.internalServiceTokenProvider = internalServiceTokenProvider;
    }

    @Override
    public ClienteIntegracao buscarCliente(Long id) {
        try {
            ClienteIntegracaoDto dto = restClient.get()
                    .uri("/cliente/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, internalServiceTokenProvider.obterAuthorizationHeader())
                    .retrieve()
                    .body(ClienteIntegracaoDto.class);
            return new ClienteIntegracao(dto.id(), dto.nome(), dto.documento(), dto.email());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ClienteNaoEncontradoException(id);
        }
    }
}
