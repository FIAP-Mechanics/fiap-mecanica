package com.fiap.mecanica.atendimento.client;

import com.fiap.mecanica.atendimento.client.dto.VeiculoIntegracaoDto;
import com.fiap.mecanica.atendimento.config.InternalServiceTokenProvider;
import com.fiap.mecanica.atendimento.exception.VeiculoInativoException;
import com.fiap.mecanica.atendimento.exception.VeiculoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class VeiculoClient {

    private final RestClient restClient;
    private final InternalServiceTokenProvider internalServiceTokenProvider;

    public VeiculoClient(
            @Value("${clients.veiculo-service.base-url}") String baseUrl,
            InternalServiceTokenProvider internalServiceTokenProvider) {
        this.restClient = RestClient.create(baseUrl);
        this.internalServiceTokenProvider = internalServiceTokenProvider;
    }

    public VeiculoIntegracaoDto buscarVeiculo(Long id) {
        try {
            return restClient.get()
                    .uri("/veiculos/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, internalServiceTokenProvider.obterAuthorizationHeader())
                    .retrieve()
                    .body(VeiculoIntegracaoDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new VeiculoNaoEncontradoException(id);
        } catch (HttpClientErrorException.BadRequest ex) {
            throw new VeiculoInativoException(id);
        }
    }
}
