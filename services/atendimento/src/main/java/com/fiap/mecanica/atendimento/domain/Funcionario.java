package com.fiap.mecanica.atendimento.domain;

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
@Table(name = "funcionario")
public class Funcionario {
    @Id()
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String email;
    private String senha;
    private String nome;
    @Enumerated(EnumType.STRING)
    private Funcao funcao;
    @Builder.Default
    private boolean ativo = true;
}
