package com.fiap.mecanica.funcionario.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fiap.mecanica.funcionario.domain.Funcao;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Dados do funcionário")
public record FuncionarioDto(
        @Schema(description = "Identificador único do funcionário", example = "1") Long id,
        @Schema(description = "E-mail do funcionário", example = "joao@mecanica.com") String email,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Schema(description = "Senha do funcionário", accessMode = Schema.AccessMode.WRITE_ONLY) String senha,
        @Schema(description = "Nome completo do funcionário", example = "João Silva") String nome,
        @Schema(description = "Cargo do funcionário") Funcao funcao
) {
}
