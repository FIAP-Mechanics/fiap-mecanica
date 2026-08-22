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
public class Insumo {

    private Long id;
    private String nome;
    private BigDecimal precoUnitario;
}
