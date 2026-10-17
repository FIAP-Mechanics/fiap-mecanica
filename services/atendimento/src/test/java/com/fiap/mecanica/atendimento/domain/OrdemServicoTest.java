package com.fiap.mecanica.atendimento.domain;

import com.fiap.mecanica.atendimento.exception.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdemServicoTest {

    // ===================== atualizarStatus =====================

    @Test
    void naoDeveAlterarHistoricoQuandoNovoStatusForIgualAoAtual() {
        OrdemServico ordemServico = criarOrdemServicoComStatus(Status.RECEBIDA);

        ordemServico.atualizarStatus(Status.RECEBIDA);

        assertThat(ordemServico.getStatus()).isEqualTo(Status.RECEBIDA);
        assertThat(ordemServico.getHistoricoDeEventos()).isEmpty();
    }

    @Test
    void deveTransicionarDeAguardandoAprovacaoParaCancelada() {
        OrdemServico ordemServico = criarOrdemServicoComStatus(Status.AGUARDANDO_APROVACAO);

        ordemServico.atualizarStatus(Status.CANCELADA);

        assertThat(ordemServico.getStatus()).isEqualTo(Status.CANCELADA);
        assertThat(ordemServico.getHistoricoDeEventos()).hasSize(1);
    }

    @Test
    void deveTransicionarDeAguardandoAprovacaoParaEmExecucao() {
        OrdemServico ordemServico = criarOrdemServicoComStatus(Status.AGUARDANDO_APROVACAO);

        ordemServico.atualizarStatus(Status.EM_EXECUCAO);

        assertThat(ordemServico.getStatus()).isEqualTo(Status.EM_EXECUCAO);
        assertThat(ordemServico.getHistoricoDeEventos()).hasSize(1);
    }

    @Test
    void deveLancarExcecaoQuandoTransicaoForInvalida() {
        OrdemServico ordemServico = criarOrdemServicoComStatus(Status.RECEBIDA);

        assertThatThrownBy(() -> ordemServico.atualizarStatus(Status.ENTREGUE))
                .isInstanceOf(TransicaoInvalidaException.class);
        assertThat(ordemServico.getHistoricoDeEventos()).isEmpty();
    }

    // ===================== tempoNoStatusAtual =====================

    @Test
    void deveCalcularTempoNoStatusAtualAPartirDaDataDeCriacaoQuandoNaoHaHistorico() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 16, 12, 0);
        OrdemServico ordemServico = criarOrdemServicoComStatus(Status.RECEBIDA);
        ordemServico.setDataCriacao(agora.minusMinutes(30));

        assertThat(ordemServico.tempoNoStatusAtual(agora)).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void deveCalcularTempoNoStatusAtualAPartirDoUltimoEventoDoHistorico() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 16, 12, 0);
        OrdemServico ordemServico = criarOrdemServicoComStatus(Status.EM_DIAGNOSTICO);
        ordemServico.setDataCriacao(agora.minusHours(5));
        ordemServico.getHistoricoDeEventos().add(TrocaStatus.builder()
                .novoStatus(Status.EM_DIAGNOSTICO)
                .dataHora(agora.minusMinutes(45))
                .build());

        assertThat(ordemServico.tempoNoStatusAtual(agora)).isEqualTo(Duration.ofMinutes(45));
    }

    @Test
    void deveUsarOEventoMaisRecenteMesmoQuandoOHistoricoVemForaDeOrdem() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 16, 12, 0);
        OrdemServico ordemServico = criarOrdemServicoComStatus(Status.AGUARDANDO_APROVACAO);
        ordemServico.getHistoricoDeEventos().add(TrocaStatus.builder()
                .novoStatus(Status.AGUARDANDO_APROVACAO)
                .dataHora(agora.minusMinutes(5))
                .build());
        ordemServico.getHistoricoDeEventos().add(TrocaStatus.builder()
                .novoStatus(Status.EM_DIAGNOSTICO)
                .dataHora(agora.minusMinutes(50))
                .build());

        assertThat(ordemServico.tempoNoStatusAtual(agora)).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void deveRetornarTempoZeroQuandoNaoHaDataDeCriacaoNemHistorico() {
        OrdemServico ordemServico = criarOrdemServicoComStatus(Status.RECEBIDA);

        assertThat(ordemServico.tempoNoStatusAtual(LocalDateTime.now())).isEqualTo(Duration.ZERO);
    }

    private OrdemServico criarOrdemServicoComStatus(Status status) {
        return OrdemServico.builder()
                .id("1")
                .status(status)
                .clienteId(1L)
                .veiculoId(1L)
                .build();
    }
}
