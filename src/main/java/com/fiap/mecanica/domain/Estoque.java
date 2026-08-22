package com.fiap.mecanica.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Estoque {

    private Long id;

    private Insumo insumo;

    private Long quantidadeInsumo;

    @Builder.Default
    private boolean ativo = true;
}
