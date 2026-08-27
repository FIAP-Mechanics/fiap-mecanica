package com.fiap.mecanica.atendimento.application.usecase;

import com.fiap.mecanica.atendimento.application.command.InsumoQuantidadeCommand;
import com.fiap.mecanica.atendimento.application.command.ServicoQuantidadeCommand;
import com.fiap.mecanica.atendimento.application.port.out.ClienteIntegracaoGateway;
import com.fiap.mecanica.atendimento.application.port.out.EstoqueIntegracaoGateway;
import com.fiap.mecanica.atendimento.application.port.out.NotificationGateway;
import com.fiap.mecanica.atendimento.application.port.out.OrdemServicoGateway;
import com.fiap.mecanica.atendimento.application.port.out.ServicoIntegracaoGateway;
import com.fiap.mecanica.atendimento.application.port.out.VeiculoIntegracaoGateway;
import com.fiap.mecanica.atendimento.domain.CodigoTemplate;
import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.OrdemServicoInsumo;
import com.fiap.mecanica.atendimento.domain.OrdemServicoServico;
import com.fiap.mecanica.atendimento.domain.Orcamento;
import com.fiap.mecanica.atendimento.domain.Status;
import com.fiap.mecanica.atendimento.domain.TempoMedioExecucaoServico;
import com.fiap.mecanica.atendimento.domain.TrocaStatus;
import com.fiap.mecanica.atendimento.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.atendimento.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.atendimento.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.atendimento.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.atendimento.exception.TransicaoInvalidaException;
import com.fiap.mecanica.atendimento.exception.ValidacaoException;
import com.fiap.mecanica.atendimento.exception.VeiculoInativoException;
import com.fiap.mecanica.atendimento.exception.VeiculoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtendimentoInteractorTest {

    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_VEICULO = 2L;
    private static final Long ID_SERVICO = 3L;
    private static final Long ID_INSUMO = 4L;
    private static final Long ID_INEXISTENTE = 99L;
    private static final String UUID_ORDEM = "550e8400-e29b-41d4-a716-446655440000";
    private static final String RELATO = "Problema relatado pelo cliente";
    private static final String EMAIL_CLIENTE = "joao@email.com";

    @Mock
    private OrdemServicoGateway ordemServicoGateway;

    @Mock
    private ClienteIntegracaoGateway clienteIntegracaoGateway;

    @Mock
    private VeiculoIntegracaoGateway veiculoIntegracaoGateway;

    @Mock
    private ServicoIntegracaoGateway servicoIntegracaoGateway;

    @Mock
    private EstoqueIntegracaoGateway estoqueIntegracaoGateway;

    @Mock
    private NotificationGateway notificationGateway;

    @InjectMocks
    private AtendimentoInteractor atendimentoInteractor;

    @Captor
    private ArgumentCaptor<OrdemServico> ordemServicoCaptor;

    // ===================== iniciarAtendimento =====================

    @Test
    void deveIniciarAtendimentoSemServicosEInsumos() {
        ClienteIntegracaoGateway.ClienteIntegracao cliente = criarCliente();
        OrdemServico ordemSalva = criarOrdemServico();

        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(cliente);
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServico resultado = atendimentoInteractor.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(UUID_ORDEM);

        verify(clienteIntegracaoGateway).buscarCliente(ID_CLIENTE);
        verify(veiculoIntegracaoGateway).buscarVeiculo(ID_VEICULO);
        verify(ordemServicoGateway).salvar(ordemServicoCaptor.capture());

        OrdemServico capturada = ordemServicoCaptor.getValue();
        assertThat(capturada.getClienteId()).isEqualTo(ID_CLIENTE);
        assertThat(capturada.getVeiculoId()).isEqualTo(ID_VEICULO);
        assertThat(capturada.getStatus()).isEqualTo(Status.RECEBIDA);
    }

    @Test
    void deveIniciarAtendimentoComServicosComSucesso() {
        ServicoIntegracaoGateway.ServicoIntegracao servico = criarServico();
        OrdemServico ordemSalva = criarOrdemServico();

        List<ServicoQuantidadeCommand> servicosRequest = List.of(new ServicoQuantidadeCommand(ID_SERVICO, 2));

        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(servicoIntegracaoGateway.buscarServico(ID_SERVICO)).thenReturn(servico);
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServico resultado = atendimentoInteractor.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, servicosRequest, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(UUID_ORDEM);

        verify(servicoIntegracaoGateway).buscarServico(ID_SERVICO);
        verify(ordemServicoGateway).salvar(any(OrdemServico.class));
        verifyNoInteractions(estoqueIntegracaoGateway);
    }

    @Test
    void deveIniciarAtendimentoComInsumosComSucesso() {
        EstoqueIntegracaoGateway.InsumoIntegracao insumo = criarInsumo();
        OrdemServico ordemSalva = criarOrdemServico();

        List<InsumoQuantidadeCommand> insumosRequest = List.of(new InsumoQuantidadeCommand(ID_INSUMO, 3));

        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(estoqueIntegracaoGateway.buscarInsumo(ID_INSUMO)).thenReturn(insumo);
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServico resultado = atendimentoInteractor.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, insumosRequest);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(UUID_ORDEM);

        verify(estoqueIntegracaoGateway).buscarInsumo(ID_INSUMO);
        verify(ordemServicoGateway).salvar(any(OrdemServico.class));
        verifyNoInteractions(servicoIntegracaoGateway);
    }

    @Test
    void deveIniciarAtendimentoComServicosEInsumosComSucesso() {
        List<ServicoQuantidadeCommand> servicosRequest = List.of(new ServicoQuantidadeCommand(ID_SERVICO, 1));
        List<InsumoQuantidadeCommand> insumosRequest = List.of(new InsumoQuantidadeCommand(ID_INSUMO, 2));

        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(servicoIntegracaoGateway.buscarServico(ID_SERVICO)).thenReturn(criarServico());
        when(estoqueIntegracaoGateway.buscarInsumo(ID_INSUMO)).thenReturn(criarInsumo());
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenReturn(criarOrdemServico());

        OrdemServico resultado = atendimentoInteractor.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, servicosRequest, insumosRequest);

        assertThat(resultado).isNotNull();
        verify(servicoIntegracaoGateway).buscarServico(ID_SERVICO);
        verify(estoqueIntegracaoGateway).buscarInsumo(ID_INSUMO);
        verify(ordemServicoGateway).salvar(any(OrdemServico.class));
    }

    @Test
    void deveLancarClienteNaoEncontradoExceptionQuandoClienteNaoExistir() {
        when(clienteIntegracaoGateway.buscarCliente(ID_INEXISTENTE))
                .thenThrow(new ClienteNaoEncontradoException(ID_INEXISTENTE));

        assertThatThrownBy(() -> atendimentoInteractor.iniciarAtendimento(ID_INEXISTENTE, ID_VEICULO, RELATO, null, null))
                .isInstanceOf(ClienteNaoEncontradoException.class);

        verify(ordemServicoGateway, never()).salvar(any());
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionQuandoVeiculoNaoExistir() {
        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        doThrow(new VeiculoNaoEncontradoException(ID_INEXISTENTE))
                .when(veiculoIntegracaoGateway).buscarVeiculo(ID_INEXISTENTE);

        assertThatThrownBy(() -> atendimentoInteractor.iniciarAtendimento(ID_CLIENTE, ID_INEXISTENTE, RELATO, null, null))
                .isInstanceOf(VeiculoNaoEncontradoException.class);

        verify(ordemServicoGateway, never()).salvar(any());
    }

    @Test
    void deveLancarVeiculoInativoExceptionQuandoVeiculoEstiverInativo() {
        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        doThrow(new VeiculoInativoException(ID_VEICULO))
                .when(veiculoIntegracaoGateway).buscarVeiculo(ID_VEICULO);

        assertThatThrownBy(() -> atendimentoInteractor.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null))
                .isInstanceOf(VeiculoInativoException.class);

        verify(ordemServicoGateway, never()).salvar(any());
    }

    @Test
    void deveLancarInsumoNaoEncontradoExceptionQuandoInsumoNaoExistir() {
        List<InsumoQuantidadeCommand> insumosRequest = List.of(new InsumoQuantidadeCommand(ID_INEXISTENTE, 1));

        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(estoqueIntegracaoGateway.buscarInsumo(ID_INEXISTENTE)).thenThrow(new InsumoNaoEncontradoException(ID_INEXISTENTE));

        assertThatThrownBy(() -> atendimentoInteractor.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, insumosRequest))
                .isInstanceOf(InsumoNaoEncontradoException.class);

        verify(ordemServicoGateway, never()).salvar(any());
    }

    // ===================== buscarPorId =====================

    @Test
    void deveBuscarOrdemServicoPorIdComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        OrdemServico resultado = atendimentoInteractor.buscarPorId(UUID_ORDEM);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(UUID_ORDEM);
        assertThat(resultado.getStatus()).isEqualTo(Status.RECEBIDA);
        assertThat(resultado.getClienteId()).isEqualTo(ID_CLIENTE);
        assertThat(resultado.getVeiculoId()).isEqualTo(ID_VEICULO);

        verify(ordemServicoGateway).buscarPorId(UUID_ORDEM);
    }

    @Test
    void deveLancarOrdemServicoNaoEncontradaExceptionQuandoIdNaoExistir() {
        String idInexistente = "id-inexistente";

        when(ordemServicoGateway.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atendimentoInteractor.buscarPorId(idInexistente))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(ordemServicoGateway).buscarPorId(idInexistente);
    }

    // ===================== listarAtendimentosEmAberto =====================

    @Test
    void deveListarAtendimentosEmAbertoComSucesso() {
        OrdemServico os1 = OrdemServico.builder()
                .status(Status.RECEBIDA)
                .clienteId(ID_CLIENTE)
                .veiculoId(ID_VEICULO)
                .build();
        OrdemServico os2 = OrdemServico.builder()
                .status(Status.EM_DIAGNOSTICO)
                .clienteId(ID_CLIENTE)
                .veiculoId(ID_VEICULO)
                .build();

        when(ordemServicoGateway.buscarTodosPorStatusNotIn(List.of(Status.ENTREGUE, Status.CANCELADA))).thenReturn(List.of(os1, os2));

        List<OrdemServico> resultado = atendimentoInteractor.listarAtendimentosEmAberto();

        assertThat(resultado).hasSize(2);
        verify(ordemServicoGateway).buscarTodosPorStatusNotIn(List.of(Status.ENTREGUE, Status.CANCELADA));
    }

    @Test
    void deveListarTempoMedioExecucaoServicosComSucesso() {
        OrdemServico ordem1 = criarOrdemServicoConcluida(
                Status.FINALIZADA, 120L,
                criarOrdemServicoServico(10L, "Troca de oleo", 1)
        );
        OrdemServico ordem2 = criarOrdemServicoConcluida(
                Status.ENTREGUE, 60L,
                criarOrdemServicoServico(10L, "Troca de oleo", 2),
                criarOrdemServicoServico(11L, "Alinhamento", 1)
        );

        when(ordemServicoGateway.buscarTodosPorStatusIn(List.of(Status.FINALIZADA, Status.ENTREGUE)))
                .thenReturn(List.of(ordem1, ordem2));

        List<TempoMedioExecucaoServico> resultado = atendimentoInteractor.listarTempoMedioExecucaoServicos();

        assertThat(resultado).hasSize(2);

        TempoMedioExecucaoServico indicadorAlinhamento = resultado.getFirst();
        assertThat(indicadorAlinhamento.getServicoId()).isEqualTo(11L);
        assertThat(indicadorAlinhamento.getNome()).isEqualTo("Alinhamento");
        assertThat(indicadorAlinhamento.getOrdensFinalizadas()).isEqualTo(1L);
        assertThat(indicadorAlinhamento.getTempoMedioExecucaoMinutos()).isEqualTo(60L);

        TempoMedioExecucaoServico indicadorTrocaOleo = resultado.get(1);
        assertThat(indicadorTrocaOleo.getServicoId()).isEqualTo(10L);
        assertThat(indicadorTrocaOleo.getNome()).isEqualTo("Troca de oleo");
        assertThat(indicadorTrocaOleo.getOrdensFinalizadas()).isEqualTo(2L);
        assertThat(indicadorTrocaOleo.getTempoMedioExecucaoMinutos()).isEqualTo(90L);

        verify(ordemServicoGateway).buscarTodosPorStatusIn(List.of(Status.FINALIZADA, Status.ENTREGUE));
    }

    @Test
    void deveIgnorarOrdensSemHistoricoDeEventosNoTempoMedioExecucaoServicos() {
        OrdemServico ordemSemHistorico = criarOrdemServicoConcluida(
                Status.FINALIZADA, null,
                criarOrdemServicoServico(ID_SERVICO, "Troca de oleo", 1)
        );

        when(ordemServicoGateway.buscarTodosPorStatusIn(List.of(Status.FINALIZADA, Status.ENTREGUE)))
                .thenReturn(List.of(ordemSemHistorico));

        List<TempoMedioExecucaoServico> resultado = atendimentoInteractor.listarTempoMedioExecucaoServicos();

        assertThat(resultado).isEmpty();
        verify(ordemServicoGateway).buscarTodosPorStatusIn(List.of(Status.FINALIZADA, Status.ENTREGUE));
    }

    @Test
    void deveIgnorarOrdensComHistoricoIncompletoNoTempoMedioExecucaoServicos() {
        OrdemServico ordemComHistoricoIncompleto = criarOrdemServicoConcluida(
                Status.FINALIZADA, null,
                criarOrdemServicoServico(ID_SERVICO, "Troca de oleo", 1)
        );
        ordemComHistoricoIncompleto.getHistoricoDeEventos().add(
                TrocaStatus.builder().novoStatus(Status.EM_EXECUCAO).dataHora(LocalDateTime.now()).build());

        when(ordemServicoGateway.buscarTodosPorStatusIn(List.of(Status.FINALIZADA, Status.ENTREGUE)))
                .thenReturn(List.of(ordemComHistoricoIncompleto));

        List<TempoMedioExecucaoServico> resultado = atendimentoInteractor.listarTempoMedioExecucaoServicos();

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveIgnorarOrdensSemOrcamentoNoTempoMedio() {
        OrdemServico ordemSemOrcamento = OrdemServico.builder()
                .id(UUID_ORDEM)
                .status(Status.FINALIZADA)
                .clienteId(ID_CLIENTE)
                .veiculoId(ID_VEICULO)
                .build();

        when(ordemServicoGateway.buscarTodosPorStatusIn(List.of(Status.FINALIZADA, Status.ENTREGUE)))
                .thenReturn(List.of(ordemSemOrcamento));

        List<TempoMedioExecucaoServico> resultado = atendimentoInteractor.listarTempoMedioExecucaoServicos();

        assertThat(resultado).isEmpty();
    }

    // ===================== iniciarDiagnostico =====================

    @Test
    void deveIniciarDiagnosticoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();
        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServico resultado = atendimentoInteractor.iniciarDiagnostico(UUID_ORDEM);

        assertThat(resultado.getStatus()).isEqualTo(Status.EM_DIAGNOSTICO);
        verify(ordemServicoGateway).salvar(any(OrdemServico.class));
    }

    // ===================== realizarDiagnostico =====================

    @Test
    void deveMoverStatusParaAguardandoAprovacaoAoFinalizarDiagnostico() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.EM_DIAGNOSTICO);
        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .servicos(new ArrayList<>())
                .insumos(new ArrayList<>())
                .build();
        ordemServico.setOrcamento(orcamento);

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServico resultado = atendimentoInteractor.realizarDiagnostico(UUID_ORDEM, null, null, "Finalizado");

        assertThat(resultado.getStatus()).isEqualTo(Status.AGUARDANDO_APROVACAO);
        verify(notificationGateway).notificarCliente(eq(CodigoTemplate.AUTORIZAR_ORCAMENTO), any(ClienteIntegracaoGateway.ClienteIntegracao.class));
        verify(ordemServicoGateway).salvar(any(OrdemServico.class));
    }

    @Test
    void deveRealizarDiagnosticoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.EM_DIAGNOSTICO);
        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .servicos(new ArrayList<>())
                .insumos(new ArrayList<>())
                .build();
        ordemServico.setOrcamento(orcamento);

        List<ServicoQuantidadeCommand> servicosRequest = List.of(new ServicoQuantidadeCommand(ID_SERVICO, 1));
        List<InsumoQuantidadeCommand> insumosRequest = List.of(new InsumoQuantidadeCommand(ID_INSUMO, 2));

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(servicoIntegracaoGateway.buscarServico(ID_SERVICO)).thenReturn(criarServico());
        when(estoqueIntegracaoGateway.buscarInsumo(ID_INSUMO)).thenReturn(criarInsumo());
        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServico resultado = atendimentoInteractor.realizarDiagnostico(UUID_ORDEM, servicosRequest, insumosRequest, "Diagnóstico concluído");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getObservacoesDiagnostico()).isEqualTo("Diagnóstico concluído");
        assertThat(resultado.getOrcamento().getServicos()).hasSize(1);
        assertThat(resultado.getOrcamento().getInsumos()).hasSize(1);
        verify(ordemServicoGateway).salvar(any(OrdemServico.class));
    }

    @Test
    void deveLancarValidacaoExceptionQuandoStatusNaoForEmDiagnosticoAoRealizarDiagnostico() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.RECEBIDA);

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        assertThatThrownBy(() -> atendimentoInteractor.realizarDiagnostico(UUID_ORDEM, null, null, null))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Apenas ordens em diagnóstico podem receber novos itens.");

        verify(ordemServicoGateway, never()).salvar(any());
    }

    @Test
    void deveAdicionarApenasServicosComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.EM_DIAGNOSTICO);
        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .servicos(new ArrayList<>())
                .insumos(new ArrayList<>())
                .build();
        ordemServico.setOrcamento(orcamento);

        List<ServicoQuantidadeCommand> servicosRequest = List.of(new ServicoQuantidadeCommand(ID_SERVICO, 1));

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(servicoIntegracaoGateway.buscarServico(ID_SERVICO)).thenReturn(criarServico());
        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServico resultado = atendimentoInteractor.realizarDiagnostico(UUID_ORDEM, servicosRequest, null, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getOrcamento().getServicos()).hasSize(1);
        assertThat(resultado.getOrcamento().getInsumos()).isEmpty();
    }

    @Test
    void deveAdicionarApenasInsumosComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.EM_DIAGNOSTICO);
        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .servicos(new ArrayList<>())
                .insumos(new ArrayList<>())
                .build();
        ordemServico.setOrcamento(orcamento);

        List<InsumoQuantidadeCommand> insumosRequest = List.of(new InsumoQuantidadeCommand(ID_INSUMO, 2));

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(estoqueIntegracaoGateway.buscarInsumo(ID_INSUMO)).thenReturn(criarInsumo());
        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServico resultado = atendimentoInteractor.realizarDiagnostico(UUID_ORDEM, null, insumosRequest, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getOrcamento().getInsumos()).hasSize(1);
        assertThat(resultado.getOrcamento().getServicos()).isEmpty();
    }

    @Test
    void deveAtualizarQuantidadeSeItemJaExistirNoOrcamento() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.EM_DIAGNOSTICO);

        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .servicos(new ArrayList<>())
                .insumos(new ArrayList<>())
                .build();
        OrdemServicoServico osServico = OrdemServicoServico.builder()
                .orcamento(orcamento)
                .servicoId(ID_SERVICO)
                .nomeServico("Troca de óleo")
                .valorUnitario(new BigDecimal("150.00"))
                .quantidade(1)
                .build();
        OrdemServicoInsumo osInsumo = OrdemServicoInsumo.builder()
                .orcamento(orcamento)
                .insumoId(ID_INSUMO)
                .nomeInsumo("Óleo de motor")
                .precoUnitario(new BigDecimal("45.90"))
                .quantidade(2)
                .build();

        orcamento.getServicos().add(osServico);
        orcamento.getInsumos().add(osInsumo);
        ordemServico.setOrcamento(orcamento);

        List<ServicoQuantidadeCommand> servicosRequest = List.of(new ServicoQuantidadeCommand(ID_SERVICO, 3));
        List<InsumoQuantidadeCommand> insumosRequest = List.of(new InsumoQuantidadeCommand(ID_INSUMO, 4));

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(servicoIntegracaoGateway.buscarServico(ID_SERVICO)).thenReturn(criarServico());
        when(estoqueIntegracaoGateway.buscarInsumo(ID_INSUMO)).thenReturn(criarInsumo());
        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        atendimentoInteractor.realizarDiagnostico(UUID_ORDEM, servicosRequest, insumosRequest, null);

        assertThat(ordemServico.getOrcamento().getServicos()).hasSize(1);
        assertThat(ordemServico.getOrcamento().getServicos().getFirst().getQuantidade()).isEqualTo(4);

        assertThat(ordemServico.getOrcamento().getInsumos()).hasSize(1);
        assertThat(ordemServico.getOrcamento().getInsumos().getFirst().getQuantidade()).isEqualTo(6);

        verify(ordemServicoGateway).salvar(any(OrdemServico.class));
    }

    @Test
    void deveEvitarDuplicatasAoIniciarAtendimentoComListaDuplicada() {
        List<ServicoQuantidadeCommand> servicosRequest = List.of(
                new ServicoQuantidadeCommand(ID_SERVICO, 1),
                new ServicoQuantidadeCommand(ID_SERVICO, 2)
        );
        List<InsumoQuantidadeCommand> insumosRequest = List.of(
                new InsumoQuantidadeCommand(ID_INSUMO, 3),
                new InsumoQuantidadeCommand(ID_INSUMO, 4)
        );

        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(servicoIntegracaoGateway.buscarServico(ID_SERVICO)).thenReturn(criarServico());
        when(estoqueIntegracaoGateway.buscarInsumo(ID_INSUMO)).thenReturn(criarInsumo());
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenReturn(criarOrdemServico());

        atendimentoInteractor.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, servicosRequest, insumosRequest);

        verify(ordemServicoGateway).salvar(ordemServicoCaptor.capture());
        OrdemServico capturada = ordemServicoCaptor.getValue();

        assertThat(capturada.getOrcamento().getServicos()).hasSize(1);
        assertThat(capturada.getOrcamento().getServicos().getFirst().getQuantidade()).isEqualTo(3);

        assertThat(capturada.getOrcamento().getInsumos()).hasSize(1);
        assertThat(capturada.getOrcamento().getInsumos().getFirst().getQuantidade()).isEqualTo(7);
    }

    // ===================== aprovarOrdemServico =====================

    @Test
    void deveAprovarOrdemServicoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.AGUARDANDO_APROVACAO);

        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .servicos(new ArrayList<>())
                .insumos(new ArrayList<>())
                .build();
        OrdemServicoInsumo osInsumo = OrdemServicoInsumo.builder()
                .orcamento(orcamento)
                .insumoId(ID_INSUMO)
                .nomeInsumo("Óleo de motor")
                .precoUnitario(new BigDecimal("45.90"))
                .quantidade(2)
                .build();
        orcamento.getInsumos().add(osInsumo);
        ordemServico.setOrcamento(orcamento);

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServico resultado = atendimentoInteractor.aprovarOrdemServico(UUID_ORDEM);

        assertThat(resultado.getStatus()).isEqualTo(Status.EM_EXECUCAO);
        assertThat(resultado.getHistoricoDeEventos()).isNotEmpty();
        assertThat(resultado.getHistoricoDeEventos().stream().anyMatch(e -> e.getNovoStatus() == Status.EM_EXECUCAO)).isTrue();
        verify(estoqueIntegracaoGateway).deduzirEstoque(anyList());
        verify(ordemServicoGateway).salvar(any(OrdemServico.class));
    }

    @Test
    void deveNotificarFuncionariosQuandoEstoqueForInsuficiente() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.AGUARDANDO_APROVACAO);

        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .servicos(new ArrayList<>())
                .insumos(new ArrayList<>())
                .build();
        OrdemServicoInsumo osInsumo = OrdemServicoInsumo.builder()
                .orcamento(orcamento)
                .insumoId(ID_INSUMO)
                .nomeInsumo("Óleo de motor")
                .precoUnitario(new BigDecimal("45.90"))
                .quantidade(20)
                .build();
        orcamento.getInsumos().add(osInsumo);
        ordemServico.setOrcamento(orcamento);

        String mensagem = "Estoque insuficiente para o insumo 'Óleo de motor'. Disponível: 5, Solicitado: 20";
        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        doThrow(new EstoqueInsuficienteException(mensagem))
                .when(estoqueIntegracaoGateway).deduzirEstoque(anyList());

        assertThatThrownBy(() -> atendimentoInteractor.aprovarOrdemServico(UUID_ORDEM))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessage(mensagem);

        verify(notificationGateway).notificarFuncionarios(CodigoTemplate.REPOSICAO_ESTOQUE, mensagem);
        verify(ordemServicoGateway, never()).salvar(any());
        assertThat(ordemServico.getStatus()).isEqualTo(Status.AGUARDANDO_APROVACAO);
    }

    @Test
    void deveAprovarOrdemServicoSemInsumosComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.AGUARDANDO_APROVACAO);

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServico resultado = atendimentoInteractor.aprovarOrdemServico(UUID_ORDEM);

        assertThat(resultado.getStatus()).isEqualTo(Status.EM_EXECUCAO);
        verify(estoqueIntegracaoGateway, never()).deduzirEstoque(anyList());
        verify(ordemServicoGateway).salvar(any(OrdemServico.class));
    }

    @Test
    void deveLancarTransicaoInvalidaExceptionQuandoStatusNaoForAguardandoAprovacao() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.RECEBIDA);

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        assertThatThrownBy(() -> atendimentoInteractor.aprovarOrdemServico(UUID_ORDEM))
                .isInstanceOf(TransicaoInvalidaException.class);

        verify(ordemServicoGateway, never()).salvar(any());
    }

    @Test
    void deveLancarOrdemServicoNaoEncontradaExceptionAoAprovarOrdemInexistente() {
        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atendimentoInteractor.aprovarOrdemServico(UUID_ORDEM))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(ordemServicoGateway, never()).salvar(any());
    }

    @Test
    void deveCancelarOrdemServicoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.AGUARDANDO_APROVACAO);

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServico resultado = atendimentoInteractor.cancelarOrdemServico(UUID_ORDEM);

        assertThat(resultado.getStatus()).isEqualTo(Status.CANCELADA);
        assertThat(resultado.getHistoricoDeEventos().stream().anyMatch(e -> e.getNovoStatus() == Status.CANCELADA)).isTrue();
        verify(estoqueIntegracaoGateway, never()).deduzirEstoque(anyList());
        verify(ordemServicoGateway).salvar(any(OrdemServico.class));
    }

    @Test
    void deveLancarTransicaoInvalidaExceptionQuandoStatusNaoPermitirCancelamento() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.EM_EXECUCAO);

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        assertThatThrownBy(() -> atendimentoInteractor.cancelarOrdemServico(UUID_ORDEM))
                .isInstanceOf(TransicaoInvalidaException.class);

        verify(ordemServicoGateway, never()).salvar(any());
    }

    @Test
    void deveLancarOrdemServicoNaoEncontradaExceptionAoCancelarOrdemInexistente() {
        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atendimentoInteractor.cancelarOrdemServico(UUID_ORDEM))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(ordemServicoGateway, never()).salvar(any());
    }

    @Test
    void deveFinalizarOrdemServicoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.EM_EXECUCAO);
        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .servicos(new ArrayList<>())
                .insumos(new ArrayList<>())
                .build();
        OrdemServicoServico servico = criarOrdemServicoServico(ID_SERVICO, "Troca de óleo", 1);
        servico.setOrcamento(orcamento);
        orcamento.getServicos().add(servico);
        ordemServico.setOrcamento(orcamento);

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServico resultado = atendimentoInteractor.finalizarOrdemServico(UUID_ORDEM);

        assertThat(resultado.getStatus()).isEqualTo(Status.FINALIZADA);
        assertThat(resultado.getHistoricoDeEventos().stream().anyMatch(e -> e.getNovoStatus() == Status.FINALIZADA)).isTrue();
        verify(notificationGateway).notificarCliente(eq(CodigoTemplate.RETIRAR_VEICULO), any(ClienteIntegracaoGateway.ClienteIntegracao.class));
        verify(ordemServicoGateway).salvar(any(OrdemServico.class));
    }

    @Test
    void deveLancarTransicaoInvalidaExceptionQuandoStatusNaoPermitirFinalizacao() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.RECEBIDA);

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        assertThatThrownBy(() -> atendimentoInteractor.finalizarOrdemServico(UUID_ORDEM))
                .isInstanceOf(TransicaoInvalidaException.class);

        verify(ordemServicoGateway, never()).salvar(any());
    }

    @Test
    void deveLancarOrdemServicoNaoEncontradaExceptionAoFinalizarOrdemInexistente() {
        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atendimentoInteractor.finalizarOrdemServico(UUID_ORDEM))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(ordemServicoGateway, never()).salvar(any());
    }

    @Test
    void deveEntregarVeiculoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.FINALIZADA);

        when(ordemServicoGateway.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(clienteIntegracaoGateway.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoGateway.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServico resultado = atendimentoInteractor.entregarVeiculo(UUID_ORDEM);

        assertThat(resultado.getStatus()).isEqualTo(Status.ENTREGUE);
        verify(notificationGateway).notificarCliente(eq(CodigoTemplate.VEICULO_RETIRADO), any(ClienteIntegracaoGateway.ClienteIntegracao.class));
        verify(ordemServicoGateway).salvar(any(OrdemServico.class));
    }

    private ClienteIntegracaoGateway.ClienteIntegracao criarCliente() {
        return new ClienteIntegracaoGateway.ClienteIntegracao(ID_CLIENTE, "João Silva", "12345678901", EMAIL_CLIENTE);
    }

    private ServicoIntegracaoGateway.ServicoIntegracao criarServico() {
        return new ServicoIntegracaoGateway.ServicoIntegracao(ID_SERVICO, "Troca de óleo", new BigDecimal("150.00"));
    }

    private EstoqueIntegracaoGateway.InsumoIntegracao criarInsumo() {
        return new EstoqueIntegracaoGateway.InsumoIntegracao(ID_INSUMO, "Óleo de motor", new BigDecimal("45.90"));
    }

    private OrdemServicoServico criarOrdemServicoServico(Long servicoId, String nome, int quantidade) {
        return OrdemServicoServico.builder()
                .servicoId(servicoId)
                .nomeServico(nome)
                .valorUnitario(new BigDecimal("150.00"))
                .quantidade(quantidade)
                .build();
    }

    private OrdemServico criarOrdemServicoConcluida(Status statusAtual, Long duracaoExecucaoMinutos, OrdemServicoServico... servicos) {
        OrdemServico ordemServico = OrdemServico.builder()
                .id(UUID_ORDEM)
                .clienteId(ID_CLIENTE)
                .veiculoId(ID_VEICULO)
                .status(statusAtual)
                .build();

        if (duracaoExecucaoMinutos != null) {
            LocalDateTime inicioExecucao = LocalDateTime.now().minusMinutes(duracaoExecucaoMinutos);
            LocalDateTime fimExecucao = inicioExecucao.plusMinutes(duracaoExecucaoMinutos);
            ordemServico.getHistoricoDeEventos().add(
                    TrocaStatus.builder().novoStatus(Status.EM_EXECUCAO).dataHora(inicioExecucao).build());
            ordemServico.getHistoricoDeEventos().add(
                    TrocaStatus.builder().novoStatus(Status.FINALIZADA).dataHora(fimExecucao).build());
        }

        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .servicos(new ArrayList<>())
                .insumos(new ArrayList<>())
                .build();
        for (OrdemServicoServico servico : servicos) {
            servico.setOrcamento(orcamento);
            orcamento.getServicos().add(servico);
        }
        ordemServico.setOrcamento(orcamento);
        return ordemServico;
    }

    private OrdemServico criarOrdemServico() {
        return OrdemServico.builder()
                .id(UUID_ORDEM)
                .clienteId(ID_CLIENTE)
                .veiculoId(ID_VEICULO)
                .status(Status.RECEBIDA)
                .build();
    }
}
