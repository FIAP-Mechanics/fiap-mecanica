package com.fiap.mecanica.atendimento.adapter.out.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Component
public class ObservabilidadeRestClientCustomizer implements RestClientCustomizer, ClientHttpRequestInterceptor {

    static final String HEADER_REQUEST_ID = "X-Request-ID";
    static final String MDC_REQUEST_ID = "request_id";

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservabilidadeRestClientCustomizer.class);

    @Override
    public void customize(RestClient.Builder restClientBuilder) {
        restClientBuilder.requestInterceptor(this);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String requestId = MDC.get(MDC_REQUEST_ID);
        if (requestId != null) {
            request.getHeaders().set(HEADER_REQUEST_ID, requestId);
        }
        try {
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode().is5xxServerError()) {
                LOGGER.warn("Integração respondeu com erro. servico={}, metodo={}, caminho={}, status={}",
                        request.getURI().getHost(), request.getMethod(), request.getURI().getPath(),
                        response.getStatusCode().value());
            }
            return response;
        } catch (IOException ex) {
            LOGGER.error("Falha de comunicação com integração. servico={}, metodo={}, caminho={}",
                    request.getURI().getHost(), request.getMethod(), request.getURI().getPath(), ex);
            throw ex;
        }
    }
}
