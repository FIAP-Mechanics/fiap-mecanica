package com.fiap.mecanica.cliente.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Dados de endereço")
public record EnderecoDto(
        @Schema(description = "CEP", example = "30140071") String cep,
        @Schema(description = "Estado (UF)", example = "MG") String estado,
        @Schema(description = "Cidade", example = "Belo Horizonte") String cidade,
        @Schema(description = "Bairro", example = "Savassi") String bairro,
        @Schema(description = "Rua / Logradouro", example = "Avenida Brasil") String rua,
        @Schema(description = "Número", example = "100") String numero,
        @Schema(description = "Complemento", example = "Apto 302") String complemento
) {
}
