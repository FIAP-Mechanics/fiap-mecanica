package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AtualizarServicoCommand;
import com.fiap.mecanica.application.port.out.ServicoGateway;
import com.fiap.mecanica.domain.Servico;
import com.fiap.mecanica.exception.ServicoInativoException;
import com.fiap.mecanica.exception.ServicoJaAtivoException;
import com.fiap.mecanica.exception.ServicoNotFound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoInteractorTest {

    @Mock
    private ServicoGateway gateway;

    @InjectMocks
    private ServicoInteractor interactor;

    @Test
    void deveCadastrarServico() {
        Servico servico = criarServico(true);
        when(gateway.salvar(servico)).thenReturn(servico);

        assertThat(interactor.cadastrarServico(servico)).isEqualTo(servico);
        verify(gateway).salvar(servico);
    }

    @Test
    void deveBuscarServicoAtivo() {
        Servico servico = criarServico(true);
        when(gateway.buscarPorId(1L)).thenReturn(Optional.of(servico));

        assertThat(interactor.buscarServicoPorId(1L)).isEqualTo(servico);
    }

    @Test
    void deveLancarNotFoundQuandoServicoNaoExistir() {
        when(gateway.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.buscarServicoPorId(99L)).isInstanceOf(ServicoNotFound.class);
    }

    @Test
    void deveLancarExcecaoQuandoServicoEstiverInativo() {
        when(gateway.buscarPorId(1L)).thenReturn(Optional.of(criarServico(false)));

        assertThatThrownBy(() -> interactor.buscarServicoPorId(1L)).isInstanceOf(ServicoInativoException.class);
    }

    @Test
    void deveAtualizarCampos() {
        Servico servico = criarServico(true);
        AtualizarServicoCommand command =
                new AtualizarServicoCommand("Troca de oleo", "Nova descricao", new BigDecimal("200.00"));
        when(gateway.buscarPorId(1L)).thenReturn(Optional.of(servico));
        when(gateway.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Servico resultado = interactor.atualizarServico(1L, command);

        assertThat(resultado.getNome()).isEqualTo("Troca de oleo");
        assertThat(resultado.getDescricao()).isEqualTo("Nova descricao");
        assertThat(resultado.getValor()).isEqualByComparingTo("200.00");
    }

    @Test
    void deveManterCamposQuandoAtualizacaoForVazia() {
        Servico servico = criarServico(true);
        when(gateway.buscarPorId(1L)).thenReturn(Optional.of(servico));
        when(gateway.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        interactor.atualizarServico(1L, new AtualizarServicoCommand(null, null, null));

        assertThat(servico.getNome()).isEqualTo("Alinhamento");
        assertThat(servico.getValor()).isEqualByComparingTo("100.00");
    }

    @Test
    void deveRealizarDeleteLogico() {
        Servico servico = criarServico(true);
        when(gateway.buscarPorId(1L)).thenReturn(Optional.of(servico));
        when(gateway.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(interactor.excluirServico(1L).isAtivo()).isFalse();
    }

    @Test
    void naoDeveSalvarAoExcluirServicoInexistente() {
        when(gateway.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.excluirServico(99L)).isInstanceOf(ServicoNotFound.class);
        verify(gateway, never()).salvar(any());
    }

    @Test
    void deveReativarServicoInativo() {
        Servico servico = criarServico(false);
        when(gateway.buscarPorId(1L)).thenReturn(Optional.of(servico));
        when(gateway.salvar(servico)).thenReturn(servico);

        assertThat(interactor.reativarServico(1L).isAtivo()).isTrue();
    }

    @Test
    void deveImpedirReativacaoDeServicoAtivo() {
        when(gateway.buscarPorId(1L)).thenReturn(Optional.of(criarServico(true)));

        assertThatThrownBy(() -> interactor.reativarServico(1L)).isInstanceOf(ServicoJaAtivoException.class);
    }

    private Servico criarServico(boolean ativo) {
        return Servico.builder()
                .id(1L)
                .nome("Alinhamento")
                .descricao("Alinhamento das rodas")
                .valor(new BigDecimal("100.00"))
                .ativo(ativo)
                .build();
    }
}
