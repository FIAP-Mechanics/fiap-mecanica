package com.fiap.mecanica.atendimento.domain;

import com.fiap.mecanica.atendimento.exception.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

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

    private OrdemServico criarOrdemServicoComStatus(Status status) {
        return OrdemServico.builder()
                .id("1")
                .status(status)
                .clienteId(1L)
                .veiculoId(1L)
                .build();
    }
}
