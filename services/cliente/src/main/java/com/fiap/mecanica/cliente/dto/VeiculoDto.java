package com.fiap.mecanica.cliente.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Dados de veículo associado")
public record VeiculoDto(
        @Schema(description = "ID do veículo", example = "1") Long id,
        @Schema(description = "Marca do veículo", example = "Toyota") String marca,
        @Schema(description = "Modelo do veículo", example = "Corolla") String modelo,
        @Schema(description = "Placa do veículo", example = "ABC1234") String placa,
        @Schema(description = "Ano do veículo", example = "2020") Integer ano
) {
}
