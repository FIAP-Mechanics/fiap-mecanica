package com.fiap.mecanica.controller.request;

import com.fiap.mecanica.domain.Funcao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CadastrarFuncionarioRequest(@NotBlank String email, @NotBlank String nome, @NotBlank String senha,
                                          @NotNull Funcao funcao) {
}