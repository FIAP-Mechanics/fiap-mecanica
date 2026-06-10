package com.fiap.mecanica.controller.request;

import com.fiap.mecanica.domain.Endereco;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Dados para atualizar cliente (todos os campos são opcionais")
public record AtualizarClienteRequest(
        @Schema(description = "Novo CPF ou CNPJ do cliente", example = "98765432111") String documento,
        @Schema(description = "Novo E-mail do clinete", example = "cliente@gmail.com") String email,
        @Schema(description = "Novo nelefone do clinete", example = "31998415627") String telefone,
        @Schema(description = "Novo endereço do clinete") Endereco endereco
) {
}
