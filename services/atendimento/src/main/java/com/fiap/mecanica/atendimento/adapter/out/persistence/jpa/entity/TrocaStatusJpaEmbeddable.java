package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity;

import com.fiap.mecanica.atendimento.domain.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TrocaStatusJpaEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status novoStatus;

    @Column(nullable = false)
    private LocalDateTime dataHora;
}
