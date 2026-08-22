package com.fiap.mecanica.veiculo.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Dados para atualização de veículo (todos os campos são opcionais)")
public record AtualizarVeiculoRequest(
        @Size(max = 50)
        @Schema(description = "Nova marca do veículo", example = "Fiat") String marca,

        @Size(max = 50)
        @Schema(description = "Novo modelo do veículo", example = "Uno") String modelo,

        @Pattern(regexp = "(?i)^(?:[A-Z]{3}[0-9]{4}|[A-Z]{3}[0-9][A-Z][0-9]{2})$", message = "Placa inválida. Utilize o padrão brasileiro ou Mercosul.")
        @Schema(description = "Nova placa do veículo", example = "ABC-1234") String placa,

        @Schema(description = "Novo ano do veículo", example = "2020") Integer ano) {
}
