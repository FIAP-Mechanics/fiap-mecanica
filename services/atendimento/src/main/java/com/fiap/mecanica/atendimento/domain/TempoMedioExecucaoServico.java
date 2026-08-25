package com.fiap.mecanica.atendimento.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempoMedioExecucaoServico {
    private Long servicoId;
    private String nome;
    private long ordensFinalizadas;
    private Long tempoMedioExecucaoMinutos;
}
