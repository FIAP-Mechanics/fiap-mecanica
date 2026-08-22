package com.fiap.mecanica.atendimento.service;

import com.fiap.mecanica.atendimento.client.ClienteClient;
import com.fiap.mecanica.atendimento.client.EstoqueClient;
import com.fiap.mecanica.atendimento.client.ServicoClient;
import com.fiap.mecanica.atendimento.client.VeiculoClient;
import com.fiap.mecanica.atendimento.client.dto.*;
import com.fiap.mecanica.atendimento.controller.request.IniciarAtendimentoRequest;
import com.fiap.mecanica.atendimento.domain.*;
import com.fiap.mecanica.atendimento.dto.OrdemServicoDto;
import com.fiap.mecanica.atendimento.dto.TempoMedioExecucaoServicoDto;
import com.fiap.mecanica.atendimento.exception.*;
import com.fiap.mecanica.atendimento.infra.enums.CodigoTemplate;
import com.fiap.mecanica.atendimento.repository.OrdemServicoRepository;
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
class OrdemServicoServiceTest {

    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_VEICULO = 2L;
    private static final Long ID_SERVICO = 3L;
    private static final Long ID_INSUMO = 4L;
    private static final Long ID_INEXISTENTE = 99L;
    private static final String UUID_ORDEM = "550e8400-e29b-41d4-a716-446655440000";
    private static final String RELATO = "Problema relatado pelo cliente";
    private static final String EMAIL_CLIENTE = "joao@email.com";

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private ClienteClient clienteClient;

    @Mock
    private VeiculoClient veiculoClient;

    @Mock
    private ServicoClient servicoClient;

    @Mock
    private EstoqueClient estoqueClient;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrdemServicoService ordemServicoService;

    @Captor
    private ArgumentCaptor<OrdemServico> ordemServicoCaptor;

    // ===================== iniciarAtendimento =====================

    @Test
    void deveIniciarAtendimentoSemServicosEInsumos() {
        ClienteIntegracaoDto cliente = criarCliente();
        VeiculoIntegracaoDto veiculo = criarVeiculo();
        OrdemServico ordemSalva = criarOrdemServico();

        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoClient.buscarVeiculo(ID_VEICULO)).thenReturn(veiculo);
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServicoDto resultado = ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);

        verify(clienteClient).buscarCliente(ID_CLIENTE);
        verify(veiculoClient).buscarVeiculo(ID_VEICULO);
        verify(ordemServicoRepository).save(ordemServicoCaptor.capture());

        OrdemServico capturada = ordemServicoCaptor.getValue();
        assertThat(capturada.getClienteId()).isEqualTo(ID_CLIENTE);
        assertThat(capturada.getVeiculoId()).isEqualTo(ID_VEICULO);
        assertThat(capturada.getStatus()).isEqualTo(Status.RECEBIDA);
    }

    @Test
    void deveIniciarAtendimentoComServicosComSucesso() {
        ServicoIntegracaoDto servico = criarServico();
        OrdemServico ordemSalva = criarOrdemServico();

        List<IniciarAtendimentoRequest.ServicoQuantidade> servicosRequest =
                List.of(new IniciarAtendimentoRequest.ServicoQuantidade(ID_SERVICO, 2));

        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(veiculoClient.buscarVeiculo(ID_VEICULO)).thenReturn(criarVeiculo());
        when(servicoClient.buscarServico(ID_SERVICO)).thenReturn(servico);
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServicoDto resultado = ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, servicosRequest, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);

        verify(servicoClient).buscarServico(ID_SERVICO);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
        verifyNoInteractions(estoqueClient);
    }

    @Test
    void deveIniciarAtendimentoComInsumosComSucesso() {
        EstoqueIntegracaoDto insumo = criarInsumo();
        OrdemServico ordemSalva = criarOrdemServico();

        List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest =
                List.of(new IniciarAtendimentoRequest.InsumoQuantidade(ID_INSUMO, 3));

        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(veiculoClient.buscarVeiculo(ID_VEICULO)).thenReturn(criarVeiculo());
        when(estoqueClient.buscarInsumo(ID_INSUMO)).thenReturn(insumo);
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServicoDto resultado = ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, insumosRequest);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);

        verify(estoqueClient).buscarInsumo(ID_INSUMO);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
        verifyNoInteractions(servicoClient);
    }

    @Test
    void deveIniciarAtendimentoComServicosEInsumosComSucesso() {
        List<IniciarAtendimentoRequest.ServicoQuantidade> servicosRequest =
                List.of(new IniciarAtendimentoRequest.ServicoQuantidade(ID_SERVICO, 1));
        List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest =
                List.of(new IniciarAtendimentoRequest.InsumoQuantidade(ID_INSUMO, 2));

        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(veiculoClient.buscarVeiculo(ID_VEICULO)).thenReturn(criarVeiculo());
        when(servicoClient.buscarServico(ID_SERVICO)).thenReturn(criarServico());
        when(estoqueClient.buscarInsumo(ID_INSUMO)).thenReturn(criarInsumo());
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(criarOrdemServico());

        OrdemServicoDto resultado = ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, servicosRequest, insumosRequest);

        assertThat(resultado).isNotNull();
        verify(servicoClient).buscarServico(ID_SERVICO);
        verify(estoqueClient).buscarInsumo(ID_INSUMO);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarClienteNaoEncontradoExceptionQuandoClienteNaoExistir() {
        when(clienteClient.buscarCliente(ID_INEXISTENTE))
                .thenThrow(new ClienteNaoEncontradoException(ID_INEXISTENTE));

        assertThatThrownBy(() -> ordemServicoService.iniciarAtendimento(ID_INEXISTENTE, ID_VEICULO, RELATO, null, null))
                .isInstanceOf(ClienteNaoEncontradoException.class);

        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionQuandoVeiculoNaoExistir() {
        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(veiculoClient.buscarVeiculo(ID_INEXISTENTE))
                .thenThrow(new VeiculoNaoEncontradoException(ID_INEXISTENTE));

        assertThatThrownBy(() -> ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_INEXISTENTE, RELATO, null, null))
                .isInstanceOf(VeiculoNaoEncontradoException.class);

        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveLancarVeiculoInativoExceptionQuandoVeiculoEstiverInativo() {
        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(veiculoClient.buscarVeiculo(ID_VEICULO)).thenThrow(new VeiculoInativoException(ID_VEICULO));

        assertThatThrownBy(() -> ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null))
                .isInstanceOf(VeiculoInativoException.class);

        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveLancarInsumoNaoEncontradoExceptionQuandoInsumoNaoExistir() {
        List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest =
                List.of(new IniciarAtendimentoRequest.InsumoQuantidade(ID_INEXISTENTE, 1));

        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(veiculoClient.buscarVeiculo(ID_VEICULO)).thenReturn(criarVeiculo());
        when(estoqueClient.buscarInsumo(ID_INEXISTENTE)).thenThrow(new InsumoNaoEncontradoException(ID_INEXISTENTE));

        assertThatThrownBy(() -> ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, insumosRequest))
                .isInstanceOf(InsumoNaoEncontradoException.class);

        verify(ordemServicoRepository, never()).save(any());
    }

    // ===================== buscarPorId =====================

    @Test
    void deveBuscarOrdemServicoPorIdComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        OrdemServicoDto resultado = ordemServicoService.buscarPorId(UUID_ORDEM);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);
        assertThat(resultado.status()).isEqualTo(Status.RECEBIDA);
        assertThat(resultado.clienteId()).isEqualTo(ID_CLIENTE);
        assertThat(resultado.veiculoId()).isEqualTo(ID_VEICULO);

        verify(ordemServicoRepository).findById(UUID_ORDEM);
    }

    @Test
    void deveLancarOrdemServicoNaoEncontradaExceptionQuandoIdNaoExistir() {
        String idInexistente = "id-inexistente";

        when(ordemServicoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordemServicoService.buscarPorId(idInexistente))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(ordemServicoRepository).findById(idInexistente);
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

        when(ordemServicoRepository.findAllByStatusNotIn(List.of(Status.ENTREGUE, Status.CANCELADA))).thenReturn(List.of(os1, os2));

        List<OrdemServicoDto> resultado = ordemServicoService.listarAtendimentosEmAberto();

        assertThat(resultado).hasSize(2);
        verify(ordemServicoRepository).findAllByStatusNotIn(List.of(Status.ENTREGUE, Status.CANCELADA));
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

        when(ordemServicoRepository.findAllByStatusIn(List.of(Status.FINALIZADA, Status.ENTREGUE)))
                .thenReturn(List.of(ordem1, ordem2));

        List<TempoMedioExecucaoServicoDto> resultado = ordemServicoService.listarTempoMedioExecucaoServicos();

        assertThat(resultado).hasSize(2);

        TempoMedioExecucaoServicoDto indicadorAlinhamento = resultado.getFirst();
        assertThat(indicadorAlinhamento.servicoId()).isEqualTo(11L);
        assertThat(indicadorAlinhamento.nome()).isEqualTo("Alinhamento");
        assertThat(indicadorAlinhamento.ordensFinalizadas()).isEqualTo(1L);
        assertThat(indicadorAlinhamento.tempoMedioExecucaoMinutos()).isEqualTo(60L);

        TempoMedioExecucaoServicoDto indicadorTrocaOleo = resultado.get(1);
        assertThat(indicadorTrocaOleo.servicoId()).isEqualTo(10L);
        assertThat(indicadorTrocaOleo.nome()).isEqualTo("Troca de oleo");
        assertThat(indicadorTrocaOleo.ordensFinalizadas()).isEqualTo(2L);
        assertThat(indicadorTrocaOleo.tempoMedioExecucaoMinutos()).isEqualTo(90L);

        verify(ordemServicoRepository).findAllByStatusIn(List.of(Status.FINALIZADA, Status.ENTREGUE));
    }

    @Test
    void deveIgnorarOrdensSemHistoricoDeEventosNoTempoMedioExecucaoServicos() {
        OrdemServico ordemSemHistorico = criarOrdemServicoConcluida(
                Status.FINALIZADA, null,
                criarOrdemServicoServico(ID_SERVICO, "Troca de oleo", 1)
        );

        when(ordemServicoRepository.findAllByStatusIn(List.of(Status.FINALIZADA, Status.ENTREGUE)))
                .thenReturn(List.of(ordemSemHistorico));

        List<TempoMedioExecucaoServicoDto> resultado = ordemServicoService.listarTempoMedioExecucaoServicos();

        assertThat(resultado).isEmpty();
        verify(ordemServicoRepository).findAllByStatusIn(List.of(Status.FINALIZADA, Status.ENTREGUE));
    }

    @Test
    void deveIgnorarOrdensComHistoricoIncompletoNoTempoMedioExecucaoServicos() {
        OrdemServico ordemComHistoricoIncompleto = criarOrdemServicoConcluida(
                Status.FINALIZADA, null,
                criarOrdemServicoServico(ID_SERVICO, "Troca de oleo", 1)
        );
        ordemComHistoricoIncompleto.getHistoricoDeEventos().add(
                TrocaStatus.builder().novoStatus(Status.EM_EXECUCAO).dataHora(LocalDateTime.now()).build());

        when(ordemServicoRepository.findAllByStatusIn(List.of(Status.FINALIZADA, Status.ENTREGUE)))
                .thenReturn(List.of(ordemComHistoricoIncompleto));

        List<TempoMedioExecucaoServicoDto> resultado = ordemServicoService.listarTempoMedioExecucaoServicos();

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

        when(ordemServicoRepository.findAllByStatusIn(List.of(Status.FINALIZADA, Status.ENTREGUE)))
                .thenReturn(List.of(ordemSemOrcamento));

        List<TempoMedioExecucaoServicoDto> resultado = ordemServicoService.listarTempoMedioExecucaoServicos();

        assertThat(resultado).isEmpty();
    }

    // ===================== iniciarDiagnostico =====================

    @Test
    void deveIniciarDiagnosticoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();
        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoDto resultado = ordemServicoService.iniciarDiagnostico(UUID_ORDEM);

        assertThat(resultado.status()).isEqualTo(Status.EM_DIAGNOSTICO);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
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

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoDto resultado = ordemServicoService.realizarDiagnostico(UUID_ORDEM, null, null, "Finalizado");

        assertThat(resultado.status()).isEqualTo(Status.AGUARDANDO_APROVACAO);
        verify(notificationService).notificarCliente(eq(CodigoTemplate.AUTORIZAR_ORCAMENTO), any(ClienteIntegracaoDto.class));
        verify(ordemServicoRepository).save(any(OrdemServico.class));
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

        List<IniciarAtendimentoRequest.ServicoQuantidade> servicosRequest =
                List.of(new IniciarAtendimentoRequest.ServicoQuantidade(ID_SERVICO, 1));
        List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest =
                List.of(new IniciarAtendimentoRequest.InsumoQuantidade(ID_INSUMO, 2));

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(servicoClient.buscarServico(ID_SERVICO)).thenReturn(criarServico());
        when(estoqueClient.buscarInsumo(ID_INSUMO)).thenReturn(criarInsumo());
        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoDto resultado = ordemServicoService.realizarDiagnostico(UUID_ORDEM, servicosRequest, insumosRequest, "Diagnóstico concluído");

        assertThat(resultado).isNotNull();
        assertThat(resultado.observacoesDiagnostico()).isEqualTo("Diagnóstico concluído");
        assertThat(resultado.orcamento().servicos()).hasSize(1);
        assertThat(resultado.orcamento().insumos()).hasSize(1);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarValidacaoExceptionQuandoStatusNaoForEmDiagnosticoAoRealizarDiagnostico() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.RECEBIDA);

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        assertThatThrownBy(() -> ordemServicoService.realizarDiagnostico(UUID_ORDEM, null, null, null))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Apenas ordens em diagnóstico podem receber novos itens.");

        verify(ordemServicoRepository, never()).save(any());
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

        List<IniciarAtendimentoRequest.ServicoQuantidade> servicosRequest =
                List.of(new IniciarAtendimentoRequest.ServicoQuantidade(ID_SERVICO, 1));

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(servicoClient.buscarServico(ID_SERVICO)).thenReturn(criarServico());
        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoDto resultado = ordemServicoService.realizarDiagnostico(UUID_ORDEM, servicosRequest, null, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.orcamento().servicos()).hasSize(1);
        assertThat(resultado.orcamento().insumos()).isEmpty();
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

        List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest =
                List.of(new IniciarAtendimentoRequest.InsumoQuantidade(ID_INSUMO, 2));

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(estoqueClient.buscarInsumo(ID_INSUMO)).thenReturn(criarInsumo());
        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoDto resultado = ordemServicoService.realizarDiagnostico(UUID_ORDEM, null, insumosRequest, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.orcamento().insumos()).hasSize(1);
        assertThat(resultado.orcamento().servicos()).isEmpty();
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

        List<IniciarAtendimentoRequest.ServicoQuantidade> servicosRequest =
                List.of(new IniciarAtendimentoRequest.ServicoQuantidade(ID_SERVICO, 3));
        List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest =
                List.of(new IniciarAtendimentoRequest.InsumoQuantidade(ID_INSUMO, 4));

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(servicoClient.buscarServico(ID_SERVICO)).thenReturn(criarServico());
        when(estoqueClient.buscarInsumo(ID_INSUMO)).thenReturn(criarInsumo());
        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ordemServicoService.realizarDiagnostico(UUID_ORDEM, servicosRequest, insumosRequest, null);

        assertThat(ordemServico.getOrcamento().getServicos()).hasSize(1);
        assertThat(ordemServico.getOrcamento().getServicos().getFirst().getQuantidade()).isEqualTo(4);

        assertThat(ordemServico.getOrcamento().getInsumos()).hasSize(1);
        assertThat(ordemServico.getOrcamento().getInsumos().getFirst().getQuantidade()).isEqualTo(6);

        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveEvitarDuplicatasAoIniciarAtendimentoComListaDuplicada() {
        List<IniciarAtendimentoRequest.ServicoQuantidade> servicosRequest = List.of(
                new IniciarAtendimentoRequest.ServicoQuantidade(ID_SERVICO, 1),
                new IniciarAtendimentoRequest.ServicoQuantidade(ID_SERVICO, 2)
        );
        List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest = List.of(
                new IniciarAtendimentoRequest.InsumoQuantidade(ID_INSUMO, 3),
                new IniciarAtendimentoRequest.InsumoQuantidade(ID_INSUMO, 4)
        );

        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(veiculoClient.buscarVeiculo(ID_VEICULO)).thenReturn(criarVeiculo());
        when(servicoClient.buscarServico(ID_SERVICO)).thenReturn(criarServico());
        when(estoqueClient.buscarInsumo(ID_INSUMO)).thenReturn(criarInsumo());
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(criarOrdemServico());

        ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, servicosRequest, insumosRequest);

        verify(ordemServicoRepository).save(ordemServicoCaptor.capture());
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

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoDto resultado = ordemServicoService.aprovarOrdemServico(UUID_ORDEM);

        assertThat(resultado.status()).isEqualTo(Status.EM_EXECUCAO);
        assertThat(resultado.historicoDeEventos()).isNotEmpty();
        assertThat(resultado.historicoDeEventos().stream().anyMatch(e -> e.status() == Status.EM_EXECUCAO)).isTrue();
        verify(estoqueClient).deduzirEstoque(anyList());
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveAprovarOrdemServicoSemInsumosComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.AGUARDANDO_APROVACAO);

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoDto resultado = ordemServicoService.aprovarOrdemServico(UUID_ORDEM);

        assertThat(resultado.status()).isEqualTo(Status.EM_EXECUCAO);
        verify(estoqueClient, never()).deduzirEstoque(anyList());
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarTransicaoInvalidaExceptionQuandoStatusNaoForAguardandoAprovacao() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.RECEBIDA);

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        assertThatThrownBy(() -> ordemServicoService.aprovarOrdemServico(UUID_ORDEM))
                .isInstanceOf(TransicaoInvalidaException.class);

        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveLancarOrdemServicoNaoEncontradaExceptionAoAprovarOrdemInexistente() {
        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordemServicoService.aprovarOrdemServico(UUID_ORDEM))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveCancelarOrdemServicoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.AGUARDANDO_APROVACAO);

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoDto resultado = ordemServicoService.cancelarOrdemServico(UUID_ORDEM);

        assertThat(resultado.status()).isEqualTo(Status.CANCELADA);
        assertThat(resultado.historicoDeEventos().stream().anyMatch(e -> e.status() == Status.CANCELADA)).isTrue();
        verify(estoqueClient, never()).deduzirEstoque(anyList());
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarTransicaoInvalidaExceptionQuandoStatusNaoPermitirCancelamento() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.EM_EXECUCAO);

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        assertThatThrownBy(() -> ordemServicoService.cancelarOrdemServico(UUID_ORDEM))
                .isInstanceOf(TransicaoInvalidaException.class);

        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveLancarOrdemServicoNaoEncontradaExceptionAoCancelarOrdemInexistente() {
        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordemServicoService.cancelarOrdemServico(UUID_ORDEM))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(ordemServicoRepository, never()).save(any());
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

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoDto resultado = ordemServicoService.finalizarOrdemServico(UUID_ORDEM);

        assertThat(resultado.status()).isEqualTo(Status.FINALIZADA);
        assertThat(resultado.historicoDeEventos().stream().anyMatch(e -> e.status() == Status.FINALIZADA)).isTrue();
        verify(notificationService).notificarCliente(eq(CodigoTemplate.RETIRAR_VEICULO), any(ClienteIntegracaoDto.class));
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarTransicaoInvalidaExceptionQuandoStatusNaoPermitirFinalizacao() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.RECEBIDA);

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        assertThatThrownBy(() -> ordemServicoService.finalizarOrdemServico(UUID_ORDEM))
                .isInstanceOf(TransicaoInvalidaException.class);

        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveLancarOrdemServicoNaoEncontradaExceptionAoFinalizarOrdemInexistente() {
        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordemServicoService.finalizarOrdemServico(UUID_ORDEM))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveEntregarVeiculoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico();
        ordemServico.setStatus(Status.FINALIZADA);

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(clienteClient.buscarCliente(ID_CLIENTE)).thenReturn(criarCliente());
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoDto resultado = ordemServicoService.entregarVeiculo(UUID_ORDEM);

        assertThat(resultado.status()).isEqualTo(Status.ENTREGUE);
        verify(notificationService).notificarCliente(eq(CodigoTemplate.VEICULO_RETIRADO), any(ClienteIntegracaoDto.class));
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    private ClienteIntegracaoDto criarCliente() {
        return new ClienteIntegracaoDto(ID_CLIENTE, "João Silva", "12345678901", EMAIL_CLIENTE);
    }

    private VeiculoIntegracaoDto criarVeiculo() {
        return new VeiculoIntegracaoDto(ID_VEICULO, "ABC1234");
    }

    private ServicoIntegracaoDto criarServico() {
        return new ServicoIntegracaoDto(ID_SERVICO, "Troca de óleo", new BigDecimal("150.00"));
    }

    private EstoqueIntegracaoDto criarInsumo() {
        return new EstoqueIntegracaoDto(
                new InsumoIntegracaoDto(ID_INSUMO, "Óleo de motor", new BigDecimal("45.90")), 10L);
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
