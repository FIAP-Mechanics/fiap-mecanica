package com.fiap.mecanica.domain;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServicoInsumo {

    private Long id;

    @ToString.Exclude
    private Orcamento orcamento;

    private Insumo insumo;

    private Integer quantidade;
}
