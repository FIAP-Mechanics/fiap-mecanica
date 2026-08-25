package com.fiap.mecanica.cliente.domain;

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

    private Long veiculoId;

    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
}
