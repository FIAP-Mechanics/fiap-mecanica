package com.fiap.mecanica.servico.application.usecase;

import com.fiap.mecanica.servico.application.command.AtualizarServicoCommand;
import com.fiap.mecanica.servico.application.port.out.ServicoGateway;
import com.fiap.mecanica.servico.domain.Servico;
import com.fiap.mecanica.servico.exception.ServicoInativoException;
import com.fiap.mecanica.servico.exception.ServicoJaAtivoException;
import com.fiap.mecanica.servico.exception.ServicoNotFound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoInteractorTest {

    @Mock
    private ServicoGateway servicoGateway;

    @InjectMocks
    private ServicoInteractor interactor;

    @Test
    void deveBuscarTodosOsServicos() {
        Servico servico = criarServico(true);
        when(servicoGateway.buscarTodos()).thenReturn(List.of(servico));

        List<Servico> resultado = interactor.buscarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst()).isEqualTo(servico);
    }

    @Test
    void deveCadastrarServico() {
        Servico servico = criarServico(true);
        when(servicoGateway.salvar(servico)).thenReturn(servico);

        assertThat(interactor.cadastrarServico(servico)).isEqualTo(servico);
        verify(servicoGateway).salvar(servico);
    }

    @Test
    void deveBuscarServicoAtivo() {
        Servico servico = criarServico(true);
        when(servicoGateway.buscarPorId(1L)).thenReturn(Optional.of(servico));

        assertThat(interactor.buscarServicoPorId(1L)).isEqualTo(servico);
    }

    @Test
    void deveLancarNotFoundQuandoServicoNaoExistir() {
        when(servicoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.buscarServicoPorId(99L)).isInstanceOf(ServicoNotFound.class);
    }

    @Test
    void deveLancarExcecaoQuandoServicoEstiverInativo() {
        when(servicoGateway.buscarPorId(1L)).thenReturn(Optional.of(criarServico(false)));

        assertThatThrownBy(() -> interactor.buscarServicoPorId(1L)).isInstanceOf(ServicoInativoException.class);
    }

    @Test
    void deveAtualizarCampos() {
        Servico servico = criarServico(true);
        AtualizarServicoCommand command = new AtualizarServicoCommand("Troca de oleo", "Nova descricao", new BigDecimal("200.00"));
        when(servicoGateway.buscarPorId(1L)).thenReturn(Optional.of(servico));
        when(servicoGateway.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Servico resultado = interactor.atualizarServico(1L, command);

        assertThat(resultado.getNome()).isEqualTo("Troca de oleo");
        assertThat(resultado.getDescricao()).isEqualTo("Nova descricao");
        assertThat(resultado.getValor()).isEqualByComparingTo("200.00");
    }

    @Test
    void deveManterCamposQuandoAtualizacaoForVazia() {
        Servico servico = criarServico(true);
        when(servicoGateway.buscarPorId(1L)).thenReturn(Optional.of(servico));
        when(servicoGateway.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        interactor.atualizarServico(1L, new AtualizarServicoCommand(null, null, null));

        assertThat(servico.getNome()).isEqualTo("Alinhamento");
        assertThat(servico.getValor()).isEqualByComparingTo("100.00");
    }

    @Test
    void deveRealizarDeleteLogico() {
        Servico servico = criarServico(true);
        when(servicoGateway.buscarPorId(1L)).thenReturn(Optional.of(servico));
        when(servicoGateway.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(interactor.excluirServico(1L).isAtivo()).isFalse();
    }

    @Test
    void naoDeveSalvarAoExcluirServicoInexistente() {
        when(servicoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.excluirServico(99L)).isInstanceOf(ServicoNotFound.class);
        verify(servicoGateway, never()).salvar(any());
    }

    @Test
    void deveReativarServicoInativo() {
        Servico servico = criarServico(false);
        when(servicoGateway.buscarPorId(1L)).thenReturn(Optional.of(servico));
        when(servicoGateway.salvar(servico)).thenReturn(servico);

        assertThat(interactor.reativarServico(1L).isAtivo()).isTrue();
    }

    @Test
    void deveImpedirReativacaoDeServicoAtivo() {
        when(servicoGateway.buscarPorId(1L)).thenReturn(Optional.of(criarServico(true)));

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
