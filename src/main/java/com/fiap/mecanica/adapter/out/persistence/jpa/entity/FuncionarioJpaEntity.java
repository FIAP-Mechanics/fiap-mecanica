package com.fiap.mecanica.adapter.out.persistence.jpa.entity;

import com.fiap.mecanica.domain.Funcao;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Funcionario")
@Table(name = "funcionario")
public class FuncionarioJpaEntity {
    @Id
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
