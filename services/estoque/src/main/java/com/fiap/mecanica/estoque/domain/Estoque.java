package com.fiap.mecanica.estoque.domain;

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
@Table(name = "estoque")
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "insumo_id", nullable = false, unique = true)
    private Insumo insumo;

    @Column(nullable = false)
    private Long quantidadeInsumo;

    @Builder.Default
    private boolean ativo = true;
}
