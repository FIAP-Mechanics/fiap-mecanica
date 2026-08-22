package com.fiap.mecanica.atendimento.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orcamento")
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ordem_servico_id", nullable = false, unique = true)
    private OrdemServico ordemServico;

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<OrdemServicoServico> servicos = new ArrayList<>();

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<OrdemServicoInsumo> insumos = new ArrayList<>();

    @Column(name = "preco_total", precision = 10, scale = 2)
    private BigDecimal precoTotal;

    public void adicionarServico(Long servicoId, String nomeServico, BigDecimal valorUnitario, Integer quantidade) {
        this.servicos.stream()
                .filter(s -> s.getServicoId().equals(servicoId))
                .findFirst()
                .ifPresentOrElse(
                        itemExistente -> itemExistente.setQuantidade(itemExistente.getQuantidade() + quantidade),
                        () -> {
                            OrdemServicoServico novoItem = OrdemServicoServico.builder()
                                    .orcamento(this)
                                    .servicoId(servicoId)
                                    .nomeServico(nomeServico)
                                    .valorUnitario(valorUnitario)
                                    .quantidade(quantidade)
                                    .build();
                            this.servicos.add(novoItem);
                        }
                );
        recalcularPrecoTotal();
    }

    public void adicionarInsumo(Long insumoId, String nomeInsumo, BigDecimal precoUnitario, Integer quantidade) {
        this.insumos.stream()
                .filter(i -> i.getInsumoId().equals(insumoId))
                .findFirst()
                .ifPresentOrElse(
                        itemExistente -> itemExistente.setQuantidade(itemExistente.getQuantidade() + quantidade),
                        () -> {
                            OrdemServicoInsumo novoItem = OrdemServicoInsumo.builder()
                                    .orcamento(this)
                                    .insumoId(insumoId)
                                    .nomeInsumo(nomeInsumo)
                                    .precoUnitario(precoUnitario)
                                    .quantidade(quantidade)
                                    .build();
                            this.insumos.add(novoItem);
                        }
                );
        recalcularPrecoTotal();
    }

    public void recalcularPrecoTotal() {
        BigDecimal totalServicos = servicos.stream()
                .map(s -> s.getValorUnitario().multiply(BigDecimal.valueOf(s.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInsumos = insumos.stream()
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.precoTotal = totalServicos.add(totalInsumos);
    }
}
