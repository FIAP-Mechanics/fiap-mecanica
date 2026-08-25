package com.fiap.mecanica.atendimento.adapter.out.client;

import com.fiap.mecanica.atendimento.adapter.out.client.dto.ServicoIntegracaoDto;
import com.fiap.mecanica.atendimento.application.port.out.ServicoIntegracaoGateway;
import com.fiap.mecanica.atendimento.exception.ServicoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class ServicoIntegracaoAdapter implements ServicoIntegracaoGateway {

    private final RestClient restClient;
    private final InternalServiceTokenProvider internalServiceTokenProvider;

    public ServicoIntegracaoAdapter(
            @Value("${clients.servico-service.base-url}") String baseUrl,
            InternalServiceTokenProvider internalServiceTokenProvider) {
        this.restClient = RestClient.create(baseUrl);
        this.internalServiceTokenProvider = internalServiceTokenProvider;
    }

    @Override
    public ServicoIntegracao buscarServico(Long id) {
        try {
            ServicoIntegracaoDto dto = restClient.get()
                    .uri("/servicos/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, internalServiceTokenProvider.obterAuthorizationHeader())
                    .retrieve()
                    .body(ServicoIntegracaoDto.class);
            return new ServicoIntegracao(dto.id(), dto.nome(), dto.valor());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ServicoNaoEncontradoException(id);
        }
    }
}
