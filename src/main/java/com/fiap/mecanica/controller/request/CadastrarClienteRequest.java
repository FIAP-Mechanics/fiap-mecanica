package com.fiap.mecanica.controller.request;

import com.fiap.mecanica.domain.Endereco;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Dados para cadastro do cliente")
public record CadastrarClienteRequest(
        @NotBlank @Schema(description = "CPF ou CNPJ do cliente", example = "12345678999") String documento,
        @NotBlank @Schema(description = "E-mail único do clinete", example = "cliente@gmail.com") String email,
        @NotBlank @Schema(description = "Telefone do clinete", example = "31998495612") String telefone,
        @NotBlank @Schema(description = "Endereço/residencia do clinete") Endereco endereco
) {
}
