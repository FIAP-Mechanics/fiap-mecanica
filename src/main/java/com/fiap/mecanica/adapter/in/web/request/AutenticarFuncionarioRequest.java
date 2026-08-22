package com.fiap.mecanica.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de funcionario para autenticacao")
public record AutenticarFuncionarioRequest(
        @NotBlank @Schema(description = "E-mail do funcionario", example = "admin@mecanica.com") String email,
        @NotBlank @Schema(description = "Senha do funcionario", example = "senha123") String senha) {
}
