package com.fiap.mecanica.atendimento.client;

import com.fiap.mecanica.atendimento.client.dto.ServicoIntegracaoDto;
import com.fiap.mecanica.atendimento.config.InternalServiceTokenProvider;
import com.fiap.mecanica.atendimento.exception.ServicoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class ServicoClient {

    private final RestClient restClient;
    private final InternalServiceTokenProvider internalServiceTokenProvider;

    public ServicoClient(
            @Value("${clients.servico-service.base-url}") String baseUrl,
            InternalServiceTokenProvider internalServiceTokenProvider) {
        this.restClient = RestClient.create(baseUrl);
        this.internalServiceTokenProvider = internalServiceTokenProvider;
    }

    public ServicoIntegracaoDto buscarServico(Long id) {
        try {
            return restClient.get()
                    .uri("/servicos/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, internalServiceTokenProvider.obterAuthorizationHeader())
                    .retrieve()
                    .body(ServicoIntegracaoDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ServicoNaoEncontradoException(id);
        }
    }
}
