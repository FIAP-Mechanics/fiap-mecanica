package com.fiap.mecanica.domain;

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

    @Column
    private java.time.LocalDateTime dataHoraAutorizacao;

    @OneToOne(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private Orcamento orcamento;
}
