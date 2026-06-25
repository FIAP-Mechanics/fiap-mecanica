package com.fiap.mecanica.infra.configs;

import com.fiap.mecanica.infra.configs.dto.ErroDetalhe;
import com.fiap.mecanica.infra.configs.dto.RespostaErro;
import com.fiap.mecanica.infra.configs.enums.CodigoErro;
import com.fiap.mecanica.exception.ConflitoException;
import com.fiap.mecanica.exception.EntidadeNaoEncontradaException;
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

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<RespostaErro> handleNotFound(
            EntidadeNaoEncontradaException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new RespostaErro(
                        List.of(
                                new ErroDetalhe(
                                        CodigoErro.ENTIDADE_NAO_ENCONTRADA.getCodigo(),
                                        ex.getMessage()
                                )
                        )
                ));
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<RespostaErro> handleConflict(
            ConflitoException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new RespostaErro(
                        List.of(
                                new ErroDetalhe(
                                        CodigoErro.CONFLITO.getCodigo(),
                                        ex.getMessage()
                                )
                        )
                ));
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<RespostaErro> handleValidation(
            ValidacaoException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new RespostaErro(
                        List.of(
                                new ErroDetalhe(
                                        CodigoErro.VALIDACAO.getCodigo(),
                                        ex.getMessage()
                                )
                        )
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaErro> handleBeanValidation(
            MethodArgumentNotValidException ex) {

        List<ErroDetalhe> erros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .map(mensagem -> new ErroDetalhe(
                        CodigoErro.VALIDACAO.getCodigo(),
                        mensagem
                ))
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new RespostaErro(erros));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RespostaErro> handleConstraintViolation(
            ConstraintViolationException ex) {

        List<ErroDetalhe> erros = ex.getConstraintViolations()
                .stream()
                .map(violation -> new ErroDetalhe(
                        CodigoErro.VALIDACAO.getCodigo(),
                        violation.getMessage()
                ))
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new RespostaErro(erros));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RespostaErro> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new RespostaErro(
                        List.of(
                                new ErroDetalhe(
                                        CodigoErro.NAO_AUTORIZADO.getCodigo(),
                                        "Credenciais invalidas."
                                )
                        )
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespostaErro> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new RespostaErro(
                        List.of(
                                new ErroDetalhe(
                                        CodigoErro.ACESSO_NEGADO.getCodigo(),
                                        "Acesso negado para esta operacao."
                                )
                        )
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErro> handleGeneric(
            Exception ex) {

        LOGGER.error("Erro interno da aplicação", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RespostaErro(
                        List.of(
                                new ErroDetalhe(
                                        CodigoErro.ERRO_INTERNO.getCodigo(),
                                        "Ocorreu um erro interno na aplicação."
                                )
                        )
                ));
    }
}
