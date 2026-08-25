package com.fiap.mecanica.atendimento.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrocaStatus {

    private Status novoStatus;

    private LocalDateTime dataHora;
}
