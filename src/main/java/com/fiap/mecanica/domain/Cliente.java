package com.fiap.mecanica.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    private Long id;
    private String nome;
    private String documento;
    private String email;
    private String telefone;
    private Endereco endereco;
}
