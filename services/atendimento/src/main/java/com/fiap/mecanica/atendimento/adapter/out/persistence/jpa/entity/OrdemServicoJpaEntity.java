package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity;

import com.fiap.mecanica.atendimento.domain.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ordem_servico")
public class OrdemServicoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "veiculo_id")
    private Long veiculoId;

    @Column
    private String relatoCliente;

    @Column
    private String observacoesDiagnostico;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @ElementCollection
    @CollectionTable(name = "ordem_servico_historico", joinColumns = @JoinColumn(name = "ordem_servico_id"))
    @Builder.Default
    private List<TrocaStatusJpaEmbeddable> historicoDeEventos = new ArrayList<>();

    @OneToOne(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private OrcamentoJpaEntity orcamento;
}
