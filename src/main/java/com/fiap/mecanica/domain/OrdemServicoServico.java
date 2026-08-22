package com.fiap.mecanica.domain;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServicoServico {

    private Long id;

    @ToString.Exclude
    private Orcamento orcamento;

    private Servico servico;

    private Integer quantidade;

    private Long tempoExecucaoMinutos;
}
