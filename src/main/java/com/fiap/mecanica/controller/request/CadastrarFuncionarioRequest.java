package com.fiap.mecanica.controller.request;

import com.fiap.mecanica.domain.Funcao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "Dados para cadastro de funcionário")
public record CadastrarFuncionarioRequest(
        @NotBlank @Schema(description = "E-mail do funcionário", example = "joao@mecanica.com") String email,
        @NotBlank @Schema(description = "Nome completo do funcionário", example = "João Silva") String nome,
        @NotBlank @Schema(description = "Senha de acesso", example = "senha123") String senha,
        @NotNull @Schema(description = "Cargo do funcionário") Funcao funcao) {
}