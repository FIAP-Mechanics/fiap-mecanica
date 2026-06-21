package com.fiap.mecanica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Dados do veículo")
public record VeiculoDto(
        @Schema(description = "Identificador único do veículo", example = "1") Long id,
        @Schema(description = "Marca do veículo", example = "Fiat") String marca,
        @Schema(description = "Modelo do veículo", example = "Uno") String modelo,
        @Schema(description = "Placa do veículo", example = "ABC-1234") String placa,
        @Schema(description = "Ano do veículo", example = "2020") Integer ano) {
}