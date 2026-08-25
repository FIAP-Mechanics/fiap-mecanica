package com.fiap.mecanica.cliente.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Dados do cliente")
public record ClienteDto(
        @Schema(description = "Identificador único do cliente", example = "1") Long id,
        @Schema(description = "Nome do cliente", example = "Jose da Silva") String nome,
        @Schema(description = "CPF/CNPJ único do cliente", example = "123.456.789-99") String documento,
        @Schema(description = "E-mail único do cliente", example = "cliente@gmail.com") String email,
        @Schema(description = "Telefone do cliente", example = "31998495612") String telefone,
        @Schema(description = "Endereço/residencia do cliente") EnderecoDto endereco
) {
}
