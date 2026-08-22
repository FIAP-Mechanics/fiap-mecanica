package com.fiap.mecanica.atendimento.client;

import com.fiap.mecanica.atendimento.client.dto.DeduzirEstoqueItemDto;
import com.fiap.mecanica.atendimento.client.dto.EstoqueIntegracaoDto;
import com.fiap.mecanica.atendimento.config.InternalServiceTokenProvider;
import com.fiap.mecanica.atendimento.exception.InsumoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class EstoqueClient {

    private final RestClient restClient;
    private final InternalServiceTokenProvider internalServiceTokenProvider;

    public EstoqueClient(
            @Value("${clients.estoque-service.base-url}") String baseUrl,
            InternalServiceTokenProvider internalServiceTokenProvider) {
        this.restClient = RestClient.create(baseUrl);
        this.internalServiceTokenProvider = internalServiceTokenProvider;
    }

    public EstoqueIntegracaoDto buscarInsumo(Long insumoId) {
        try {
            return restClient.get()
                    .uri("/estoque/{insumoId}", insumoId)
                    .header(HttpHeaders.AUTHORIZATION, internalServiceTokenProvider.obterAuthorizationHeader())
                    .retrieve()
                    .body(EstoqueIntegracaoDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new InsumoNaoEncontradoException(insumoId);
        }
    }

    public void deduzirEstoque(List<DeduzirEstoqueItemDto> itens) {
        restClient.post()
                .uri("/estoque/deduzir")
                .header(HttpHeaders.AUTHORIZATION, internalServiceTokenProvider.obterAuthorizationHeader())
                .body(itens)
                .retrieve()
                .toBodilessEntity();
    }
}
