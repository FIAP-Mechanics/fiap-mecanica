package com.fiap.mecanica.controller.request;

import com.fiap.mecanica.domain.Endereco;
import com.fiap.mecanica.validation.CpfOuCnpjValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(description = "Dados para atualizar cliente (todos os campos são opcionais")
public record AtualizarClienteRequest(
        @NotBlank @Schema(description = "NOme do cliente", example = "Jose da Silva") String nome,
        @NotBlank @CpfOuCnpjValido
        @Schema(description = "Novo CPF ou CNPJ do cliente", example = "529.982.247-25") String documento,
        @NotBlank @Schema(description = "Novo E-mail do clinete", example = "cliente@gmail.com") String email,
        @Pattern(
                regexp = "^(\\d{10,11}|\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4})$",
                message = "Telefone inválido"
        ) @NotBlank @Schema(description = "Novo telefone do clinete", example = "31998415627") String telefone,
        @NotNull @Schema(description = "Novo endereço do clinete") Endereco endereco
) {
}
