package com.fiap.mecanica.estoque.infra;

import com.fiap.mecanica.estoque.exception.ConflitoException;
import com.fiap.mecanica.estoque.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.estoque.exception.EstoqueNotFound;
import com.fiap.mecanica.estoque.exception.ValidacaoException;
import com.fiap.mecanica.estoque.infra.dto.RespostaErro;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveRetornar404QuandoEntidadeNaoEncontrada() {
        ResponseEntity<RespostaErro> response = handler.handleNotFound(new EstoqueNotFound(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().erros().getFirst().codigo()).isEqualTo("entidade-nao-encontrada");
    }

    @Test
    void deveRetornar409QuandoHouverConflito() {
        ConflitoException conflito = new ConflitoException("Erro de conflito") {
        };
        ResponseEntity<RespostaErro> response = handler.handleConflict(conflito);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().erros().getFirst().codigo()).isEqualTo("conflito");
    }

    @Test
    void deveRetornar400QuandoHouverErroDeValidacao() {
        ResponseEntity<RespostaErro> response = handler.handleValidation(new ValidacaoException("Erro de validacao") {
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().erros().getFirst().codigo()).isEqualTo("validacao");
    }

    @Test
    void deveRetornar400QuandoEstoqueForInsuficiente() {
        ResponseEntity<RespostaErro> response = handler.handleEstoqueInsuficiente(new EstoqueInsuficienteException("Insumo", 10L, 5L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().erros().getFirst().codigo()).isEqualTo("validacao");
    }

    @Test
    void deveRetornar401QuandoHouverErroDeAutenticacao() {
        AuthenticationException authEx = mock(AuthenticationException.class);
        ResponseEntity<RespostaErro> response = handler.handleAuthentication(authEx);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().erros().getFirst().codigo()).isEqualTo("nao-autorizado");
    }

    @Test
    void deveRetornar403QuandoAcessoForNegado() {
        AccessDeniedException accessEx = new AccessDeniedException("Acesso negado");
        ResponseEntity<RespostaErro> response = handler.handleAccessDenied(accessEx);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().erros().getFirst().codigo()).isEqualTo("acesso-negado");
    }

    @Test
    void deveRetornar500QuandoHouverErroGenerico() {
        ResponseEntity<RespostaErro> response = handler.handleGeneric(new RuntimeException("Erro genérico"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().erros().getFirst().codigo()).isEqualTo("erro-interno");
    }
}
