package com.fiap.mecanica.adapter.out.persistence.jpa.entity;

import com.fiap.mecanica.domain.Status;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "OrdemServico")
@Table(name = "ordem_servico")
public class OrdemServicoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private ClienteJpaEntity cliente;

    @ManyToOne
    @JoinColumn(name = "veiculo_id")
    private VeiculoJpaEntity veiculo;

    @Column
    private String relatoCliente;

    @Column
    private String observacoesDiagnostico;

    @Setter(AccessLevel.NONE)
    @ElementCollection
    @CollectionTable(name = "ordem_servico_historico", joinColumns = @JoinColumn(name = "ordem_servico_id"))
    @Builder.Default
    private List<TrocaStatusJpaEmbeddable> historicoDeEventos = new ArrayList<>();

    @OneToOne(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private OrcamentoJpaEntity orcamento;
}
