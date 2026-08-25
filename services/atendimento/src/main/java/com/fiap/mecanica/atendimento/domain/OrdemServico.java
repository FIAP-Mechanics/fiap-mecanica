package com.fiap.mecanica.atendimento.domain;

import com.fiap.mecanica.atendimento.exception.TransicaoInvalidaException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServico {

    private String id;

    private Status status;

    private Long clienteId;

    private Long veiculoId;

    private String relatoCliente;

    private String observacoesDiagnostico;

    @Setter(AccessLevel.NONE)
    @Builder.Default
    private List<TrocaStatus> historicoDeEventos = new ArrayList<>();

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
