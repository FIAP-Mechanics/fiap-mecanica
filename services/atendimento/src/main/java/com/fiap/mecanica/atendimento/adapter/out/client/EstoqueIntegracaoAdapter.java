package com.fiap.mecanica.atendimento.adapter.out.client;

import com.fiap.mecanica.atendimento.adapter.out.client.dto.DeduzirEstoqueItemDto;
import com.fiap.mecanica.atendimento.adapter.out.client.dto.EstoqueIntegracaoDto;
import com.fiap.mecanica.atendimento.application.port.out.EstoqueIntegracaoGateway;
import com.fiap.mecanica.atendimento.exception.InsumoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class EstoqueIntegracaoAdapter implements EstoqueIntegracaoGateway {

    private final RestClient restClient;
    private final InternalServiceTokenProvider internalServiceTokenProvider;

    public EstoqueIntegracaoAdapter(
            @Value("${clients.estoque-service.base-url}") String baseUrl,
            InternalServiceTokenProvider internalServiceTokenProvider) {
        this.restClient = RestClient.create(baseUrl);
        this.internalServiceTokenProvider = internalServiceTokenProvider;
    }

    @Override
    public InsumoIntegracao buscarInsumo(Long insumoId) {
        try {
            EstoqueIntegracaoDto dto = restClient.get()
                    .uri("/estoque/{insumoId}", insumoId)
                    .header(HttpHeaders.AUTHORIZATION, internalServiceTokenProvider.obterAuthorizationHeader())
                    .retrieve()
                    .body(EstoqueIntegracaoDto.class);
            return new InsumoIntegracao(dto.insumo().id(), dto.insumo().nome(), dto.insumo().precoUnitario());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new InsumoNaoEncontradoException(insumoId);
        }
    }

    @Override
    public void deduzirEstoque(List<ItemDeducaoEstoque> itens) {
        List<DeduzirEstoqueItemDto> itensDto = itens.stream()
                .map(item -> new DeduzirEstoqueItemDto(item.insumoId(), item.quantidade()))
                .toList();

        restClient.post()
                .uri("/estoque/deduzir")
                .header(HttpHeaders.AUTHORIZATION, internalServiceTokenProvider.obterAuthorizationHeader())
                .body(itensDto)
                .retrieve()
                .toBodilessEntity();
    }
}
