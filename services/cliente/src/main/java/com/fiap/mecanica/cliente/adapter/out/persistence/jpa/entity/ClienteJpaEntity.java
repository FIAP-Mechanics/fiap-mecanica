package com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity;

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
@Table(name = "cliente")
public class ClienteJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String nome;
    @Column(unique = true)
    private String documento;
    private String email;
    private String telefone;
    @Embedded
    private EnderecoJpaEmbeddable endereco;
    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private boolean ativo = true;
}
