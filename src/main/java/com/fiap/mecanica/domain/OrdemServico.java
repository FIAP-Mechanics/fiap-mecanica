package com.fiap.mecanica.domain;

import com.fiap.mecanica.exception.TransicaoInvalidaException;
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
    @ToString.Exclude
    private Orcamento orcamento;

    public void atualizarStatus(Status novoStatus) {
        if (this.status == novoStatus) return;
        validarTransicao(novoStatus);
        this.status = novoStatus;
        TrocaStatus evento = TrocaStatus.builder()
                .novoStatus(novoStatus)
                .dataHora(LocalDateTime.now())
                .build();
        this.historicoDeEventos.add(evento);
    }

    private void validarTransicao(Status novoStatus) {
        if (this.status == novoStatus) return;

        boolean transicaoValida = switch (this.status) {
            case RECEBIDA -> novoStatus == Status.EM_DIAGNOSTICO;
            case EM_DIAGNOSTICO -> novoStatus == Status.AGUARDANDO_APROVACAO;
            case AGUARDANDO_APROVACAO -> novoStatus == Status.EM_EXECUCAO || novoStatus == Status.CANCELADA;
            case EM_EXECUCAO -> novoStatus == Status.FINALIZADA;
            case FINALIZADA -> novoStatus == Status.ENTREGUE;
            default -> false;
        };

        if (!transicaoValida) {
            throw new TransicaoInvalidaException(this.status, novoStatus);
        }
    }
}
