package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.InsumoQuantidadeCommand;
import com.fiap.mecanica.application.command.ServicoQuantidadeCommand;
import com.fiap.mecanica.application.command.ServicoTempoCommand;
import com.fiap.mecanica.application.port.in.ClienteUseCase;
import com.fiap.mecanica.application.port.in.EstoqueUseCase;
import com.fiap.mecanica.application.port.in.InsumoUseCase;
import com.fiap.mecanica.application.port.in.ServicoUseCase;
import com.fiap.mecanica.application.port.in.VeiculoUseCase;
import com.fiap.mecanica.application.port.out.NotificacaoGateway;
import com.fiap.mecanica.application.port.out.OrdemServicoGateway;
import com.fiap.mecanica.application.result.TempoMedioExecucaoServicoResult;
import com.fiap.mecanica.domain.*;
import com.fiap.mecanica.domain.exception.TransicaoInvalidaException;
import com.fiap.mecanica.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
class AtendimentoInteractorTest {

    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_VEICULO = 2L;
    private static final Long ID_SERVICO = 3L;
    private static final Long ID_INSUMO = 4L;
    private static final Long ID_INEXISTENTE = 99L;
    private static final String UUID_ORDEM = "550e8400-e29b-41d4-a716-446655440000";
    private static final String RELATO = "Problema relatado pelo cliente";

    @Mock
    private OrdemServicoGateway ordemServicoRepository;

    @Mock
    private ClienteUseCase clienteService;

    @Mock
    private VeiculoUseCase veiculoService;

    @Mock
    private ServicoUseCase servicoService;

    @Mock
    private InsumoUseCase insumoService;

    @Mock
    private EstoqueUseCase estoqueService;

    @Mock
    private NotificacaoGateway emailNotificationService;

    @InjectMocks
    private AtendimentoInteractor ordemServicoService;

    @Captor
    private ArgumentCaptor<OrdemServico> ordemServicoCaptor;

    // ===================== iniciarAtendimento =====================

    @Test
    void deveIniciarAtendimentoSemServicosEInsumos() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemSalva = criarOrdemServico(cliente, veiculo);

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_VEICULO)).thenReturn(veiculo);
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServico resultado = ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(UUID_ORDEM);

        verify(clienteService).buscarClientePorId(ID_CLIENTE);
        verify(veiculoService).buscarVeiculoPorId(ID_VEICULO);
        verify(ordemServicoRepository).salvar(ordemServicoCaptor.capture());

        OrdemServico capturada = ordemServicoCaptor.getValue();
        assertThat(capturada.getCliente()).isEqualTo(cliente);
        assertThat(capturada.getVeiculo()).isEqualTo(veiculo);
        assertThat(capturada.getStatus()).isEqualTo(Status.RECEBIDA);
    }

    @Test
    void deveIniciarAtendimentoComServicosComSucesso() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        Servico servico = criarServico();
        OrdemServico ordemSalva = criarOrdemServico(cliente, veiculo);

        List<ServicoQuantidadeCommand> servicosRequest =
                List.of(new ServicoQuantidadeCommand(ID_SERVICO, 2));

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_VEICULO)).thenReturn(veiculo);
        when(servicoService.buscarServicoPorId(ID_SERVICO)).thenReturn(servico);
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServico resultado = ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, servicosRequest, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(UUID_ORDEM);

        verify(servicoService).buscarServicoPorId(ID_SERVICO);
        verify(ordemServicoRepository).salvar(any(OrdemServico.class));
        verifyNoInteractions(insumoService);
    }

    @Test
    void deveIniciarAtendimentoComInsumosComSucesso() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        Insumo insumo = criarInsumo();
        OrdemServico ordemSalva = criarOrdemServico(cliente, veiculo);

        List<InsumoQuantidadeCommand> insumosRequest =
                List.of(new InsumoQuantidadeCommand(ID_INSUMO, 3));

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_VEICULO)).thenReturn(veiculo);
        when(insumoService.buscarInsumoPorId(ID_INSUMO)).thenReturn(insumo);
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServico resultado = ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, insumosRequest);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(UUID_ORDEM);

        verify(insumoService).buscarInsumoPorId(ID_INSUMO);
        verify(ordemServicoRepository).salvar(any(OrdemServico.class));
        verifyNoInteractions(servicoService);
    }

    @Test
    void deveIniciarAtendimentoComServicosEInsumosComSucesso() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        Servico servico = criarServico();
        Insumo insumo = criarInsumo();
        OrdemServico ordemSalva = criarOrdemServico(cliente, veiculo);

        List<ServicoQuantidadeCommand> servicosRequest =
                List.of(new ServicoQuantidadeCommand(ID_SERVICO, 1));
        List<InsumoQuantidadeCommand> insumosRequest =
                List.of(new InsumoQuantidadeCommand(ID_INSUMO, 2));

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_VEICULO)).thenReturn(veiculo);
        when(servicoService.buscarServicoPorId(ID_SERVICO)).thenReturn(servico);
        when(insumoService.buscarInsumoPorId(ID_INSUMO)).thenReturn(insumo);
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServico resultado = ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, servicosRequest, insumosRequest);

        assertThat(resultado).isNotNull();
        verify(servicoService).buscarServicoPorId(ID_SERVICO);
        verify(insumoService).buscarInsumoPorId(ID_INSUMO);
        verify(ordemServicoRepository).salvar(any(OrdemServico.class));
    }

    @Test
    void deveLancarClienteNotFoundQuandoClienteNaoExistir() {
        when(clienteService.buscarClientePorId(ID_INEXISTENTE))
                .thenThrow(new ClienteNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> ordemServicoService.iniciarAtendimento(ID_INEXISTENTE, ID_VEICULO, RELATO, null, null))
                .isInstanceOf(ClienteNotFound.class);

        verify(ordemServicoRepository, never()).salvar(any());
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionQuandoVeiculoNaoExistir() {
        Cliente cliente = criarCliente();

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_INEXISTENTE))
                .thenThrow(new VeiculoNaoEncontradoException(ID_INEXISTENTE));

        assertThatThrownBy(() -> ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_INEXISTENTE, RELATO, null, null))
                .isInstanceOf(VeiculoNaoEncontradoException.class);

        verify(ordemServicoRepository, never()).salvar(any());
    }

    @Test
    void deveLancarVeiculoInativoExceptionQuandoVeiculoEstiverInativo() {
        Cliente cliente = criarCliente();

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_VEICULO))
                .thenThrow(new VeiculoInativoException(ID_VEICULO));

        assertThatThrownBy(() -> ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null))
                .isInstanceOf(VeiculoInativoException.class);

        verify(ordemServicoRepository, never()).salvar(any());
    }

    @Test
    void deveLancarInsumoNotFoundQuandoInsumoNaoExistir() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();

        List<InsumoQuantidadeCommand> insumosRequest =
                List.of(new InsumoQuantidadeCommand(ID_INEXISTENTE, 1));

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_VEICULO)).thenReturn(veiculo);
        when(insumoService.buscarInsumoPorId(ID_INEXISTENTE)).thenThrow(new InsumoNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, insumosRequest))
                .isInstanceOf(InsumoNotFound.class);

        verify(ordemServicoRepository, never()).salvar(any());
    }

    // ===================== buscarPorId =====================

    @Test
    void deveBuscarOrdemServicoPorIdComSucesso() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        OrdemServico resultado = ordemServicoService.buscarPorId(UUID_ORDEM);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(UUID_ORDEM);
        assertThat(resultado.getStatus()).isEqualTo(Status.RECEBIDA);
        assertThat(resultado.getCliente()).isNotNull();
        assertThat(resultado.getVeiculo()).isNotNull();

        verify(ordemServicoRepository).buscarPorId(UUID_ORDEM);
    }

    @Test
    void deveLancarOrdemServicoNaoEncontradaExceptionQuandoIdNaoExistir() {
        String idInexistente = "id-inexistente";

        when(ordemServicoRepository.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordemServicoService.buscarPorId(idInexistente))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(ordemServicoRepository).buscarPorId(idInexistente);
    }

    // ===================== listarAtendimentosEmAberto =====================

    @Test
    void deveListarAtendimentosEmAbertoComSucesso() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico os1 = OrdemServico.builder().status(Status.RECEBIDA).cliente(cliente).veiculo(veiculo).build();
        OrdemServico os2 = OrdemServico.builder().status(Status.EM_DIAGNOSTICO).cliente(cliente).veiculo(veiculo).build();

        when(ordemServicoRepository.buscarPorStatusForaDe(List.of(Status.ENTREGUE, Status.CANCELADA))).thenReturn(List.of(os1, os2));

        // Act
        List<OrdemServico> resultado = ordemServicoService.listarAtendimentosEmAberto();

        // Assert
        assertThat(resultado).hasSize(2);
        verify(ordemServicoRepository).buscarPorStatusForaDe(List.of(Status.ENTREGUE, Status.CANCELADA));
    }

    @Test
    void deveListarTempoMedioExecucaoServicosComSucesso() {
        Servico trocaOleo = criarServico(10L, "Troca de oleo");
        Servico alinhamento = criarServico(11L, "Alinhamento");

        OrdemServico ordem1 = criarOrdemServicoConcluida(
                Status.FINALIZADA,
                criarOrdemServicoServico(trocaOleo, 1, 120L)
        );
        OrdemServico ordem2 = criarOrdemServicoConcluida(
                Status.ENTREGUE,
                criarOrdemServicoServico(trocaOleo, 2, 60L),
                criarOrdemServicoServico(alinhamento, 1, 60L)
        );

        when(ordemServicoRepository.buscarPorStatusEm(List.of(Status.FINALIZADA, Status.ENTREGUE)))
                .thenReturn(List.of(ordem1, ordem2));

        List<TempoMedioExecucaoServicoResult> resultado = ordemServicoService.listarTempoMedioExecucaoServicos();

        assertThat(resultado).hasSize(2);

        TempoMedioExecucaoServicoResult indicadorAlinhamento = resultado.get(0);
        assertThat(indicadorAlinhamento.servicoId()).isEqualTo(11L);
        assertThat(indicadorAlinhamento.nome()).isEqualTo("Alinhamento");
        assertThat(indicadorAlinhamento.ordensFinalizadas()).isEqualTo(1L);
        assertThat(indicadorAlinhamento.tempoMedioExecucaoMinutos()).isEqualByComparingTo(new BigDecimal("60.00"));

        TempoMedioExecucaoServicoResult indicadorTrocaOleo = resultado.get(1);
        assertThat(indicadorTrocaOleo.servicoId()).isEqualTo(10L);
        assertThat(indicadorTrocaOleo.nome()).isEqualTo("Troca de oleo");
        assertThat(indicadorTrocaOleo.ordensFinalizadas()).isEqualTo(2L);
        assertThat(indicadorTrocaOleo.tempoMedioExecucaoMinutos()).isEqualByComparingTo(new BigDecimal("90.00"));

        verify(ordemServicoRepository).buscarPorStatusEm(List.of(Status.FINALIZADA, Status.ENTREGUE));
    }

    @Test
    void deveIgnorarServicosSemTempoRegistradoNoTempoMedioExecucaoServicos() {
        OrdemServico ordemSemTempo = criarOrdemServicoConcluida(
                Status.FINALIZADA,
                criarOrdemServicoServico(criarServico(), 1)
        );

        when(ordemServicoRepository.buscarPorStatusEm(List.of(Status.FINALIZADA, Status.ENTREGUE)))
                .thenReturn(List.of(ordemSemTempo));

        List<TempoMedioExecucaoServicoResult> resultado = ordemServicoService.listarTempoMedioExecucaoServicos();

        assertThat(resultado).isEmpty();
        verify(ordemServicoRepository).buscarPorStatusEm(List.of(Status.FINALIZADA, Status.ENTREGUE));
    }

    // ===================== realizarDiagnostico =====================

    @Test
    void deveMoverStatusParaAguardandoAprovacaoAoFinalizarDiagnostico() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);
        ordemServico.setStatus(Status.EM_DIAGNOSTICO);
        Orcamento orcamento = Orcamento.builder().ordemServico(ordemServico).build();
        ordemServico.setOrcamento(orcamento);

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrdemServico resultado = ordemServicoService.realizarDiagnostico(UUID_ORDEM, null, null, "Finalizado");

        // Assert
        assertThat(resultado.getStatus()).isEqualTo(Status.AGUARDANDO_APROVACAO);
        verify(ordemServicoRepository).salvar(any(OrdemServico.class));
    }

    @Test
    void deveRealizarDiagnosticoComSucesso() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);
        ordemServico.setStatus(Status.EM_DIAGNOSTICO);
        Orcamento orcamento = Orcamento.builder().ordemServico(ordemServico).build();
        ordemServico.setOrcamento(orcamento);

        Servico servico = criarServico();
        Insumo insumo = criarInsumo();

        List<ServicoQuantidadeCommand> servicosRequest =
                List.of(new ServicoQuantidadeCommand(ID_SERVICO, 1));
        List<InsumoQuantidadeCommand> insumosRequest =
                List.of(new InsumoQuantidadeCommand(ID_INSUMO, 2));

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(servicoService.buscarServicoPorId(ID_SERVICO)).thenReturn(servico);
        when(insumoService.buscarInsumoPorId(ID_INSUMO)).thenReturn(insumo);
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrdemServico resultado = ordemServicoService.realizarDiagnostico(UUID_ORDEM, servicosRequest, insumosRequest, "Diagnóstico concluído");

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getObservacoesDiagnostico()).isEqualTo("Diagnóstico concluído");
        assertThat(resultado.getOrcamento().getServicos()).hasSize(1);
        assertThat(resultado.getOrcamento().getInsumos()).hasSize(1);
        verify(ordemServicoRepository).salvar(any(OrdemServico.class));
    }

    @Test
    void deveLancarValidacaoExceptionQuandoStatusNaoForEmDiagnosticoAoRealizarDiagnostico() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);
        ordemServico.setStatus(Status.RECEBIDA);

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        // Act & Assert
        assertThatThrownBy(() -> ordemServicoService.realizarDiagnostico(UUID_ORDEM, null, null, null))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Apenas ordens em diagnóstico podem receber novos itens.");

        verify(ordemServicoRepository, never()).salvar(any());
    }

    @Test
    void deveAdicionarApenasServicosComSucesso() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);
        ordemServico.setStatus(Status.EM_DIAGNOSTICO);
        Orcamento orcamento = Orcamento.builder().ordemServico(ordemServico).build();
        ordemServico.setOrcamento(orcamento);

        Servico servico = criarServico();

        List<ServicoQuantidadeCommand> servicosRequest =
                List.of(new ServicoQuantidadeCommand(ID_SERVICO, 1));

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(servicoService.buscarServicoPorId(ID_SERVICO)).thenReturn(servico);
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrdemServico resultado = ordemServicoService.realizarDiagnostico(UUID_ORDEM, servicosRequest, null, null);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getOrcamento().getServicos()).hasSize(1);
        assertThat(resultado.getOrcamento().getInsumos()).isEmpty();
    }

    @Test
    void deveAdicionarApenasInsumosComSucesso() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);
        ordemServico.setStatus(Status.EM_DIAGNOSTICO);
        Orcamento orcamento = Orcamento.builder().ordemServico(ordemServico).build();
        ordemServico.setOrcamento(orcamento);

        Insumo insumo = criarInsumo();

        List<InsumoQuantidadeCommand> insumosRequest =
                List.of(new InsumoQuantidadeCommand(ID_INSUMO, 2));

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(insumoService.buscarInsumoPorId(ID_INSUMO)).thenReturn(insumo);
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrdemServico resultado = ordemServicoService.realizarDiagnostico(UUID_ORDEM, null, insumosRequest, null);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getOrcamento().getInsumos()).hasSize(1);
        assertThat(resultado.getOrcamento().getServicos()).isEmpty();
    }

    @Test
    void deveAtualizarQuantidadeSeItemJaExistirNoOrcamento() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);
        ordemServico.setStatus(Status.EM_DIAGNOSTICO);

        Servico servico = criarServico();
        Insumo insumo = criarInsumo();

        Orcamento orcamento = Orcamento.builder().ordemServico(ordemServico).build();
        OrdemServicoServico osServico = OrdemServicoServico.builder()
                .orcamento(orcamento)
                .servico(servico)
                .quantidade(1)
                .build();
        OrdemServicoInsumo osInsumo = OrdemServicoInsumo.builder()
                .orcamento(orcamento)
                .insumo(insumo)
                .quantidade(2)
                .build();

        orcamento.getServicos().add(osServico);
        orcamento.getInsumos().add(osInsumo);
        ordemServico.setOrcamento(orcamento);

        List<ServicoQuantidadeCommand> servicosRequest =
                List.of(new ServicoQuantidadeCommand(ID_SERVICO, 3));
        List<InsumoQuantidadeCommand> insumosRequest =
                List.of(new InsumoQuantidadeCommand(ID_INSUMO, 4));

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(servicoService.buscarServicoPorId(ID_SERVICO)).thenReturn(servico);
        when(insumoService.buscarInsumoPorId(ID_INSUMO)).thenReturn(insumo);
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ordemServicoService.realizarDiagnostico(UUID_ORDEM, servicosRequest, insumosRequest, null);

        // Assert
        assertThat(ordemServico.getOrcamento().getServicos()).hasSize(1);
        assertThat(ordemServico.getOrcamento().getServicos().getFirst().getQuantidade()).isEqualTo(4); // 1 + 3

        assertThat(ordemServico.getOrcamento().getInsumos()).hasSize(1);
        assertThat(ordemServico.getOrcamento().getInsumos().getFirst().getQuantidade()).isEqualTo(6); // 2 + 4

        verify(ordemServicoRepository).salvar(any(OrdemServico.class));
    }

    @Test
    void deveEvitarDuplicatasAoIniciarAtendimentoComListaDuplicada() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        Servico servico = criarServico();
        Insumo insumo = criarInsumo();
        OrdemServico ordemSalva = criarOrdemServico(cliente, veiculo);

        List<ServicoQuantidadeCommand> servicosRequest = List.of(
                new ServicoQuantidadeCommand(ID_SERVICO, 1),
                new ServicoQuantidadeCommand(ID_SERVICO, 2)
        );
        List<InsumoQuantidadeCommand> insumosRequest = List.of(
                new InsumoQuantidadeCommand(ID_INSUMO, 3),
                new InsumoQuantidadeCommand(ID_INSUMO, 4)
        );

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_VEICULO)).thenReturn(veiculo);
        when(servicoService.buscarServicoPorId(ID_SERVICO)).thenReturn(servico);
        when(insumoService.buscarInsumoPorId(ID_INSUMO)).thenReturn(insumo);
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenReturn(ordemSalva);

        // Act
        ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, servicosRequest, insumosRequest);

        // Assert
        verify(ordemServicoRepository).salvar(ordemServicoCaptor.capture());
        OrdemServico capturada = ordemServicoCaptor.getValue();

        assertThat(capturada.getOrcamento().getServicos()).hasSize(1);
        assertThat(capturada.getOrcamento().getServicos().getFirst().getQuantidade()).isEqualTo(3);

        assertThat(capturada.getOrcamento().getInsumos()).hasSize(1);
        assertThat(capturada.getOrcamento().getInsumos().getFirst().getQuantidade()).isEqualTo(7);
    }

    // ===================== aprovarOrdemServico =====================

    @Test
    void deveAprovarOrdemServicoComSucesso() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);
        ordemServico.setStatus(Status.AGUARDANDO_APROVACAO);

        Orcamento orcamento = Orcamento.builder().ordemServico(ordemServico).build();
        Insumo insumo = criarInsumo();
        OrdemServicoInsumo osInsumo = OrdemServicoInsumo.builder()
                .orcamento(orcamento)
                .insumo(insumo)
                .quantidade(2)
                .build();
        orcamento.getInsumos().add(osInsumo);
        ordemServico.setOrcamento(orcamento);

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrdemServico resultado = ordemServicoService.aprovarOrdemServico(UUID_ORDEM);

        // Assert
        assertThat(resultado.getStatus()).isEqualTo(Status.EM_EXECUCAO);
        assertThat(resultado.getHistoricoDeEventos()).isNotEmpty();
        assertThat(resultado.getHistoricoDeEventos().stream().anyMatch(e -> e.getNovoStatus() == Status.EM_EXECUCAO)).isTrue();
        verify(estoqueService).deduzirEstoque(anyList());
        verify(ordemServicoRepository).salvar(any(OrdemServico.class));
    }

    @Test
    void deveAprovarOrdemServicoSemInsumosComSucesso() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);
        ordemServico.setStatus(Status.AGUARDANDO_APROVACAO);

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrdemServico resultado = ordemServicoService.aprovarOrdemServico(UUID_ORDEM);

        // Assert
        assertThat(resultado.getStatus()).isEqualTo(Status.EM_EXECUCAO);
        verifyNoInteractions(estoqueService);
        verify(ordemServicoRepository).salvar(any(OrdemServico.class));
    }

    @Test
    void deveLancarTransicaoInvalidaExceptionQuandoStatusNaoForAguardandoAprovacao() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);
        ordemServico.setStatus(Status.RECEBIDA);

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        // Act & Assert
        assertThatThrownBy(() -> ordemServicoService.aprovarOrdemServico(UUID_ORDEM))
                .isInstanceOf(TransicaoInvalidaException.class);

        verify(ordemServicoRepository, never()).salvar(any());
    }

    @Test
    void deveLancarOrdemServicoNaoEncontradaExceptionAoAprovarOrdemInexistente() {
        // Arrange
        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> ordemServicoService.aprovarOrdemServico(UUID_ORDEM))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(ordemServicoRepository, never()).salvar(any());
    }

    @Test
    void deveCancelarOrdemServicoComSucesso() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);
        ordemServico.setStatus(Status.AGUARDANDO_APROVACAO);

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrdemServico resultado = ordemServicoService.cancelarOrdemServico(UUID_ORDEM);

        // Assert
        assertThat(resultado.getStatus()).isEqualTo(Status.CANCELADA);
        assertThat(resultado.getHistoricoDeEventos().stream().anyMatch(e -> e.getNovoStatus() == Status.CANCELADA)).isTrue();
        verifyNoInteractions(estoqueService);
        verify(ordemServicoRepository).salvar(any(OrdemServico.class));
    }

    @Test
    void deveLancarTransicaoInvalidaExceptionQuandoStatusNaoPermitirCancelamento() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);
        ordemServico.setStatus(Status.EM_EXECUCAO);

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        // Act & Assert
        assertThatThrownBy(() -> ordemServicoService.cancelarOrdemServico(UUID_ORDEM))
                .isInstanceOf(TransicaoInvalidaException.class);

        verify(ordemServicoRepository, never()).salvar(any());
    }

    @Test
    void deveLancarOrdemServicoNaoEncontradaExceptionAoCancelarOrdemInexistente() {
        // Arrange
        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> ordemServicoService.cancelarOrdemServico(UUID_ORDEM))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(ordemServicoRepository, never()).salvar(any());
    }

    @Test
    void deveFinalizarOrdemServicoComSucesso() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);
        ordemServico.setStatus(Status.EM_EXECUCAO);
        Orcamento orcamento = Orcamento.builder().ordemServico(ordemServico).build();
        OrdemServicoServico servico = criarOrdemServicoServico(criarServico(), 1);
        servico.setOrcamento(orcamento);
        orcamento.getServicos().add(servico);
        ordemServico.setOrcamento(orcamento);

        List<ServicoTempoCommand> servicosTempo =
                List.of(new ServicoTempoCommand(ID_SERVICO, 90L));

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrdemServico resultado = ordemServicoService.finalizarOrdemServico(UUID_ORDEM, servicosTempo);

        // Assert
        assertThat(resultado.getStatus()).isEqualTo(Status.FINALIZADA);
        assertThat(resultado.getOrcamento().getServicos().getFirst().getTempoExecucaoMinutos()).isEqualTo(90L);
        verify(emailNotificationService).notificarCliente(eq(CodigoTemplate.RETIRAR_VEICULO), eq(cliente));
        verify(ordemServicoRepository).salvar(any(OrdemServico.class));
    }

    @Test
    void deveEntregarVeiculoComSucesso() {
        // Arrange
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);
        ordemServico.setStatus(Status.FINALIZADA);

        when(ordemServicoRepository.buscarPorId(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrdemServico resultado = ordemServicoService.entregarVeiculo(UUID_ORDEM);

        // Assert
        assertThat(resultado.getStatus()).isEqualTo(Status.ENTREGUE);
        verify(emailNotificationService).notificarCliente(eq(CodigoTemplate.VEICULO_RETIRADO), eq(cliente));
        verify(ordemServicoRepository).salvar(any(OrdemServico.class));
    }

    private Cliente criarCliente() {
        return Cliente.builder()
                .id(ID_CLIENTE)
                .nome("João Silva")
                .documento("12345678901")
                .email("joao@email.com")
                .telefone("11999999999")
                .build();
    }

    private Veiculo criarVeiculoAtivo() {
        return Veiculo.builder()
                .id(ID_VEICULO)
                .marca("Fiat")
                .modelo("Uno")
                .placa("ABC1234")
                .ano(2020)
                .ativo(true)
                .build();
    }

    private Servico criarServico() {
        return Servico.builder()
                .id(ID_SERVICO)
                .nome("Troca de óleo")
                .descricao("Troca completa do óleo do motor")
                .valor(new BigDecimal("150.00"))
                .ativo(true)
                .build();
    }

    private Insumo criarInsumo() {
        return Insumo.builder()
                .id(ID_INSUMO)
                .nome("Óleo de motor")
                .precoUnitario(new BigDecimal("45.90"))
                .build();
    }

    private Servico criarServico(Long id, String nome) {
        return Servico.builder()
                .id(id)
                .nome(nome)
                .descricao(nome)
                .valor(new BigDecimal("150.00"))
                .ativo(true)
                .build();
    }

    private OrdemServicoServico criarOrdemServicoServico(Servico servico, int quantidade) {
        return OrdemServicoServico.builder()
                .servico(servico)
                .quantidade(quantidade)
                .build();
    }

    private OrdemServicoServico criarOrdemServicoServico(Servico servico, int quantidade, Long tempoExecucaoMinutos) {
        return OrdemServicoServico.builder()
                .servico(servico)
                .quantidade(quantidade)
                .tempoExecucaoMinutos(tempoExecucaoMinutos)
                .build();
    }

    private OrdemServico criarOrdemServicoConcluida(Status statusAtual, OrdemServicoServico... servicos) {
        OrdemServico ordemServico = OrdemServico.builder()
                .id(UUID_ORDEM)
                .cliente(criarCliente())
                .veiculo(criarVeiculoAtivo())
                .status(statusAtual)
                .build();

        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .build();
        for (OrdemServicoServico servico : servicos) {
            servico.setOrcamento(orcamento);
            orcamento.getServicos().add(servico);
        }
        ordemServico.setOrcamento(orcamento);
        return ordemServico;
    }

    private OrdemServico criarOrdemServico(Cliente cliente, Veiculo veiculo) {
        return OrdemServico.builder()
                .id(UUID_ORDEM)
                .cliente(cliente)
                .veiculo(veiculo)
                .status(Status.RECEBIDA)
                .build();
    }
}
