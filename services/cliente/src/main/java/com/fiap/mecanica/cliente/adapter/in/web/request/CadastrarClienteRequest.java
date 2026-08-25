package com.fiap.mecanica.cliente.adapter.in.web.request;

import com.fiap.mecanica.cliente.adapter.in.web.validation.CpfOuCnpjValido;
import com.fiap.mecanica.cliente.domain.Endereco;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(description = "Dados para cadastro do cliente")
public record CadastrarClienteRequest(
        @NotBlank @Schema(description = "Nome do cliente", example = "Jose da Silva") String nome,
        @NotBlank @CpfOuCnpjValido
        @Schema(description = "CPF ou CNPJ do cliente", example = "529.982.247-25") String documento,
        @NotBlank @Schema(description = "E-mail único do cliente", example = "cliente@gmail.com") String email,
        @NotBlank @Pattern(
                regexp = "^(\\d{10,11}|\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4})$",
                message = "Telefone inválido"
        ) @Schema(description = "Telefone do cliente", example = "31998495612") String telefone,
        @NotNull @Schema(description = "Endereço/residencia do cliente") Endereco endereco
) {
}
