package com.fiap.mecanica.domain;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orcamento {

    private Long id;

    private OrdemServico ordemServico;

    @Builder.Default
    @ToString.Exclude
    private List<OrdemServicoServico> servicos = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    private List<OrdemServicoInsumo> insumos = new ArrayList<>();

    private BigDecimal precoTotal;

    public void adicionarServico(Servico servico, Integer quantidade) {
        this.servicos.stream()
                .filter(s -> s.getServico().getId().equals(servico.getId()))
                .findFirst()
                .ifPresentOrElse(
                        itemExistente -> itemExistente.setQuantidade(itemExistente.getQuantidade() + quantidade),
                        () -> {
                            OrdemServicoServico novoItem = OrdemServicoServico.builder()
                                    .orcamento(this)
                                    .servico(servico)
                                    .quantidade(quantidade)
                                    .build();
                            this.servicos.add(novoItem);
                        }
                );
        recalcularPrecoTotal();
    }

    public void adicionarInsumo(Insumo insumo, Integer quantidade) {
        this.insumos.stream()
                .filter(i -> i.getInsumo().getId().equals(insumo.getId()))
                .findFirst()
                .ifPresentOrElse(
                        itemExistente -> itemExistente.setQuantidade(itemExistente.getQuantidade() + quantidade),
                        () -> {
                            OrdemServicoInsumo novoItem = OrdemServicoInsumo.builder()
                                    .orcamento(this)
                                    .insumo(insumo)
                                    .quantidade(quantidade)
                                    .build();
                            this.insumos.add(novoItem);
                        }
                );
        recalcularPrecoTotal();
    }

    public void recalcularPrecoTotal() {
        BigDecimal totalServicos = servicos.stream()
                .map(s -> s.getServico().getValor()
                        .multiply(BigDecimal.valueOf(s.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInsumos = insumos.stream()
                .map(i -> i.getInsumo().getPrecoUnitario()
                        .multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.precoTotal = totalServicos.add(totalInsumos);
    }
}
