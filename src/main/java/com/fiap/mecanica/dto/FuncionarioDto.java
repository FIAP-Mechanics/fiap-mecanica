package com.fiap.mecanica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fiap.mecanica.domain.Funcao;
import lombok.Builder;

@Builder
public record FuncionarioDto(Long id, String email, @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
String senha, String nome, Funcao funcao) {
}
