package com.fiap.mecanica.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Servico {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal valor;
    @Builder.Default
    private boolean ativo = true;
}
