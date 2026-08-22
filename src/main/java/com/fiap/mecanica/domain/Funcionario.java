package com.fiap.mecanica.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Funcionario {
    private Long id;
    private String email;
    private String senha;
    private String nome;
    private Funcao funcao;
    @Builder.Default
    private boolean ativo = true;
}
