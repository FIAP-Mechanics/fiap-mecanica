package com.fiap.mecanica.controller.request;

import com.fiap.mecanica.domain.Funcao;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para atualização de funcionário (todos os campos são opcionais)")
public record AtualizarFuncionarioRequest(
        @Schema(description = "Novo e-mail do funcionário", example = "joao@mecanica.com") String email,
        @Schema(description = "Novo nome do funcionário", example = "João Silva") String nome,
        @Schema(description = "Nova senha de acesso", example = "novaSenha123") String senha,
        @Schema(description = "Novo cargo do funcionário") Funcao funcao) {
}
