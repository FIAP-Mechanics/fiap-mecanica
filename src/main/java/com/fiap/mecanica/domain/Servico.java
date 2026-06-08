package com.fiap.mecanica.domain;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "servico")
public class Servico {

    @Id()
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String nome;
    @Column(columnDefinition = "TEXT")
    private String descricao;
    @Column(precision = 10, scale = 2)
    private BigDecimal valor;
    @Builder.Default
    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicoInsumo> insumos = new ArrayList<>();
    @Builder.Default
    private boolean ativo = true;

    public void atualizarInsumos(List<ServicoInsumo> novosInsumos) {
        insumos.clear();
        novosInsumos.forEach(this::adicionarInsumo);
    }

    private void adicionarInsumo(ServicoInsumo servicoInsumo) {
        servicoInsumo.setServico(this);
        insumos.add(servicoInsumo);
    }
}
