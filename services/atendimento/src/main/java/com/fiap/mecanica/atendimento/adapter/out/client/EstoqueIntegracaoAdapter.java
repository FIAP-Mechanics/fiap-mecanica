package com.fiap.mecanica.atendimento.adapter.out.client;

import com.fiap.mecanica.atendimento.adapter.out.client.dto.DeduzirEstoqueItemDto;
import com.fiap.mecanica.atendimento.adapter.out.client.dto.EstoqueIntegracaoDto;
import com.fiap.mecanica.atendimento.adapter.out.client.dto.RespostaErroIntegracaoDto;
import com.fiap.mecanica.atendimento.application.port.out.EstoqueIntegracaoGateway;
import com.fiap.mecanica.atendimento.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.atendimento.exception.InsumoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class EstoqueIntegracaoAdapter implements EstoqueIntegracaoGateway {

    private static final String CODIGO_ESTOQUE_INSUFICIENTE = "estoque-insuficiente";

    private final RestClient restClient;
    private final InternalServiceTokenProvider internalServiceTokenProvider;
    private final ObjectMapper objectMapper;

    public EstoqueIntegracaoAdapter(
            @Value("${clients.estoque-service.base-url}") String baseUrl,
            InternalServiceTokenProvider internalServiceTokenProvider,
            ObjectMapper objectMapper) {
        this.restClient = RestClient.create(baseUrl);
        this.internalServiceTokenProvider = internalServiceTokenProvider;
        this.objectMapper = objectMapper;
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

        try {
            restClient.post()
                    .uri("/estoque/deduzir")
                    .header(HttpHeaders.AUTHORIZATION, internalServiceTokenProvider.obterAuthorizationHeader())
                    .body(itensDto)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest ex) {
            RespostaErroIntegracaoDto.ErroIntegracaoDto erro = buscarErroEstoqueInsuficiente(ex);
            if (erro != null) {
                throw new EstoqueInsuficienteException(erro.descricao());
            }
            throw ex;
        }
    }

    private RespostaErroIntegracaoDto.ErroIntegracaoDto buscarErroEstoqueInsuficiente(
            HttpClientErrorException.BadRequest ex) {
        try {
            RespostaErroIntegracaoDto resposta = objectMapper.readValue(
                    ex.getResponseBodyAsByteArray(), RespostaErroIntegracaoDto.class);
            if (resposta.erros() == null) {
                return null;
            }
            return resposta.erros().stream()
                    .filter(erro -> CODIGO_ESTOQUE_INSUFICIENTE.equals(erro.codigo()))
                    .findFirst()
                    .orElse(null);
        } catch (JacksonException ignored) {
            return null;
        }
    }
}
