package com.fiap.mecanica.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Dados para cadastro de veículo")
public record CadastrarVeiculoRequest(
        @NotBlank @Size(max = 50) @Schema(description = "Marca do veículo", example = "Fiat") String marca,
        @NotBlank @Size(max = 50) @Schema(description = "Modelo do veículo", example = "Uno") String modelo,
        @NotBlank
        @Pattern(regexp = "(?i)^(?:[A-Z]{3}[0-9]{4}|[A-Z]{3}[0-9][A-Z][0-9]{2})$", message = "Placa inválida. Utilize o padrão brasileiro ou Mercosul.")
        @Schema(description = "Placa do veículo", example = "ABC-1234") String placa,
        @NotNull @Schema(description = "Ano do veículo", example = "2020") Integer ano) {
}
