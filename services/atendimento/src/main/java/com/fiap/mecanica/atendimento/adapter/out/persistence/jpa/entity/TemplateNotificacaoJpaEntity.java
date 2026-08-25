package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "template_notificacao")
public class TemplateNotificacaoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;
}
