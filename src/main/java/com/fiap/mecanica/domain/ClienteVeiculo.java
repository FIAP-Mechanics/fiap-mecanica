package com.fiap.mecanica.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteVeiculo {

    private Long id;

    private Cliente cliente;

    private Veiculo veiculo;
}
