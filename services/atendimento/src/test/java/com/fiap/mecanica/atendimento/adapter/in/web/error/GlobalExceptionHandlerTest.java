package com.fiap.mecanica.atendimento.adapter.in.web.error;

import com.fiap.mecanica.atendimento.exception.ConflitoException;
import com.fiap.mecanica.atendimento.exception.ValidacaoException;
import com.fiap.mecanica.atendimento.exception.VeiculoInativoException;
import com.fiap.mecanica.atendimento.exception.VeiculoNaoEncontradoException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveHandleEntidadeNaoEncontradaExceptionComStatusNotFound() {
        VeiculoNaoEncontradoException exception = new VeiculoNaoEncontradoException(1L);

        ResponseEntity<RespostaErro> response = handler.handleNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().erros()).hasSize(1);
        assertThat(response.getBody().erros().getFirst().codigo())
                .isEqualTo(CodigoErro.ENTIDADE_NAO_ENCONTRADA.getCodigo());
    }

    @Test
    void deveHandleConflitoExceptionComStatusConflict() {
        ConflitoException exception = new VeiculoInativoException(1L);

        ResponseEntity<RespostaErro> response = handler.handleConflict(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().erros()).hasSize(1);
        assertThat(response.getBody().erros().getFirst().codigo())
                .isEqualTo(CodigoErro.CONFLITO.getCodigo());
    }

    @Test
    void deveHandleValidacaoExceptionComStatusBadRequest() {
        ValidacaoException exception = new ValidacaoException("Campo inválido");

        ResponseEntity<RespostaErro> response = handler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().erros()).hasSize(1);
        assertThat(response.getBody().erros().getFirst().codigo())
                .isEqualTo(CodigoErro.VALIDACAO.getCodigo());
        assertThat(response.getBody().erros().getFirst().descricao())
                .isEqualTo("Campo inválido");
    }

    @Test
    void deveHandleMethodArgumentNotValidExceptionComMultiplosErros() {
        FieldError fieldError1 = mock(FieldError.class);
        FieldError fieldError2 = mock(FieldError.class);

        when(fieldError1.getDefaultMessage()).thenReturn("Campo 1 é obrigatório");
        when(fieldError2.getDefaultMessage()).thenReturn("Campo 2 deve ser válido");

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(mock(org.springframework.validation.BindingResult.class));
        when(exception.getBindingResult().getFieldErrors())
                .thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<RespostaErro> response = handler.handleBeanValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().erros()).hasSize(2);
        assertThat(response.getBody().erros().getFirst().codigo())
                .isEqualTo(CodigoErro.VALIDACAO.getCodigo());
        assertThat(response.getBody().erros().getFirst().descricao())
                .isEqualTo("Campo 1 é obrigatório");
    }

    @Test
    void deveHandleMethodArgumentNotValidExceptionComErroVazio() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(mock(org.springframework.validation.BindingResult.class));
        when(exception.getBindingResult().getFieldErrors())
                .thenReturn(List.of());

        ResponseEntity<RespostaErro> response = handler.handleBeanValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().erros()).isEmpty();
    }

    @Test
    void deveHandleConstraintViolationExceptionComStatusBadRequest() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Violação de constraint");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<RespostaErro> response = handler.handleConstraintViolation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().erros()).hasSize(1);
        assertThat(response.getBody().erros().getFirst().codigo())
                .isEqualTo(CodigoErro.VALIDACAO.getCodigo());
        assertThat(response.getBody().erros().getFirst().descricao())
                .isEqualTo("Violação de constraint");
    }

    @Test
    void deveHandleConstraintViolationExceptionComMultiplasViolacoes() {
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);

        when(violation1.getMessage()).thenReturn("Violação 1");
        when(violation2.getMessage()).thenReturn("Violação 2");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation1);
        violations.add(violation2);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<RespostaErro> response = handler.handleConstraintViolation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().erros()).hasSize(2);
    }

    @Test
    void deveHandleAuthenticationExceptionComStatusUnauthorized() {
        ResponseEntity<RespostaErro> response = handler.handleAuthentication(new BadCredentialsException("bad"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().erros().getFirst().codigo())
                .isEqualTo(CodigoErro.NAO_AUTORIZADO.getCodigo());
    }

    @Test
    void deveHandleAccessDeniedExceptionComStatusForbidden() {
        ResponseEntity<RespostaErro> response = handler.handleAccessDenied(new AccessDeniedException("denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().erros().getFirst().codigo())
                .isEqualTo(CodigoErro.ACESSO_NEGADO.getCodigo());
    }

    @Test
    void deveHandleGenericExceptionComStatusInternalServerError() {
        Exception exception = new RuntimeException("Erro genérico");

        ResponseEntity<RespostaErro> response = handler.handleGeneric(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().erros()).hasSize(1);
        assertThat(response.getBody().erros().getFirst().codigo())
                .isEqualTo(CodigoErro.ERRO_INTERNO.getCodigo());
        assertThat(response.getBody().erros().getFirst().descricao())
                .isEqualTo("Ocorreu um erro interno na aplicação.");
    }

    @Test
    void deveHandleGenericExceptionComMensagemGenericaDespiteOriginalMessage() {
        Exception exception = new IllegalArgumentException("Alguma mensagem específica");

        ResponseEntity<RespostaErro> response = handler.handleGeneric(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().erros().getFirst().descricao())
                .isEqualTo("Ocorreu um erro interno na aplicação.");
    }
}
