package com.fiap.mecanica.veiculo.adapter.in.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-ID";
    public static final String MDC_KEY = "request_id";

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestIdFilter.class);
    private static final Pattern FORMATO_VALIDO = Pattern.compile("[A-Za-z0-9._-]{1,128}");
    private static final String PREFIXO_ACTUATOR = "/actuator";
    private static final String ROTA_DESCONHECIDA = "desconhecida";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = resolverRequestId(request.getHeader(HEADER));
        MDC.put(MDC_KEY, requestId);
        response.setHeader(HEADER, requestId);
        long inicio = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            registrarRequisicao(request, response, inicio);
            MDC.remove(MDC_KEY);
        }
    }

    private void registrarRequisicao(HttpServletRequest request, HttpServletResponse response, long inicio) {
        if (request.getRequestURI().startsWith(PREFIXO_ACTUATOR)) {
            return;
        }
        Object rota = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        LOGGER.atInfo()
                .addKeyValue("metodo", request.getMethod())
                .addKeyValue("rota", rota != null ? rota.toString() : ROTA_DESCONHECIDA)
                .addKeyValue("status", response.getStatus())
                .addKeyValue("duracao_ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio))
                .log("Requisição concluída");
    }

    private String resolverRequestId(String valor) {
        if (valor != null && FORMATO_VALIDO.matcher(valor).matches()) {
            return valor;
        }
        return UUID.randomUUID().toString();
    }
}
