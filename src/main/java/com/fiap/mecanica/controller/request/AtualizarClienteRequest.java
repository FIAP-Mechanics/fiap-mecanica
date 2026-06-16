package com.fiap.mecanica.controller.request;

import com.fiap.mecanica.domain.Endereco;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(description = "Dados para atualizar cliente (todos os campos são opcionais")
public record AtualizarClienteRequest(
        @NotBlank @Schema(description = "NOme do cliente", example = "Jose da Silva") String nome,
        @NotBlank @Pattern(
                regexp = "^(\\d{11}|\\d{14}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})$",
                message = "Documento deve ser um CPF ou CNPJ válido"
        ) @Schema(description = "Novo CPF ou CNPJ do cliente", example = "98765432111") String documento,
        @NotBlank @Schema(description = "Novo E-mail do clinete", example = "cliente@gmail.com") String email,
        @Pattern(
                regexp = "^(\\d{10,11}|\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4})$",
                message = "Telefone inválido"
        ) @NotBlank @Schema(description = "Novo telefone do clinete", example = "31998415627") String telefone,
        @NotNull @Schema(description = "Novo endereço do clinete") Endereco endereco
) {
}
