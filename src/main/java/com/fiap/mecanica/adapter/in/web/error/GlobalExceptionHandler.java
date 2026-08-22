package com.fiap.mecanica.adapter.in.web.error;

import com.fiap.mecanica.exception.ConflitoException;
import com.fiap.mecanica.exception.EntidadeNaoEncontradaException;
import com.fiap.mecanica.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.exception.ValidacaoException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<RespostaErro> handleNotFound(EntidadeNaoEncontradaException ex) {
        return resposta(HttpStatus.NOT_FOUND, CodigoErro.ENTIDADE_NAO_ENCONTRADA, ex.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<RespostaErro> handleConflict(ConflitoException ex) {
        return resposta(HttpStatus.CONFLICT, CodigoErro.CONFLITO, ex.getMessage());
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<RespostaErro> handleValidation(ValidacaoException ex) {
        return resposta(HttpStatus.BAD_REQUEST, CodigoErro.VALIDACAO, ex.getMessage());
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<RespostaErro> handleEstoqueInsuficiente(EstoqueInsuficienteException ex) {
        return resposta(HttpStatus.BAD_REQUEST, CodigoErro.VALIDACAO, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaErro> handleBeanValidation(MethodArgumentNotValidException ex) {
        List<ErroDetalhe> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .map(mensagem -> new ErroDetalhe(CodigoErro.VALIDACAO.getCodigo(), mensagem))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RespostaErro(erros));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RespostaErro> handleConstraintViolation(ConstraintViolationException ex) {
        List<ErroDetalhe> erros = ex.getConstraintViolations().stream()
                .map(violation -> new ErroDetalhe(CodigoErro.VALIDACAO.getCodigo(), violation.getMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RespostaErro(erros));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RespostaErro> handleAuthentication(AuthenticationException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, CodigoErro.NAO_AUTORIZADO, "Credenciais invalidas.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespostaErro> handleAccessDenied(AccessDeniedException ex) {
        return resposta(HttpStatus.FORBIDDEN, CodigoErro.ACESSO_NEGADO, "Acesso negado para esta operacao.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErro> handleGeneric(Exception ex) {
        LOGGER.error("Erro interno da aplicação", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, CodigoErro.ERRO_INTERNO,
                "Ocorreu um erro interno na aplicação.");
    }

    private ResponseEntity<RespostaErro> resposta(HttpStatus status, CodigoErro codigo, String descricao) {
        return ResponseEntity.status(status)
                .body(new RespostaErro(List.of(new ErroDetalhe(codigo.getCodigo(), descricao))));
    }
}
