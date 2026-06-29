package com.fiap.mecanica.domain;

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
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @Column
    private String relatoCliente;

    @Column
    private String observacoesDiagnostico;

    @Setter(AccessLevel.NONE)
    @ElementCollection
    @CollectionTable(name = "ordem_servico_historico", joinColumns = @JoinColumn(name = "ordem_servico_id"))
    @Builder.Default
    private List<TrocaStatus> historicoDeEventos = new ArrayList<>();

    @OneToOne(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private Orcamento orcamento;

    public void setStatus(Status novoStatus) {
        this.status = novoStatus;
        TrocaStatus evento = TrocaStatus.builder()
                .status(novoStatus)
                .dataHora(LocalDateTime.now())
                .build();
        this.historicoDeEventos.add(evento);
    }
}
