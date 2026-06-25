package com.fiap.mecanica.service;

import com.fiap.mecanica.controller.request.IniciarAtendimentoRequest;
import com.fiap.mecanica.domain.*;
import com.fiap.mecanica.dto.OrdemServicoDto;
import com.fiap.mecanica.exception.*;
import com.fiap.mecanica.repository.OrdemServicoRepository;
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
class OrdemServicoServiceTest {

    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_VEICULO = 2L;
    private static final Long ID_SERVICO = 3L;
    private static final Long ID_INSUMO = 4L;
    private static final Long ID_INEXISTENTE = 99L;
    private static final String UUID_ORDEM = "550e8400-e29b-41d4-a716-446655440000";
    private static final String RELATO = "Problema relatado pelo cliente";

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private VeiculoService veiculoService;

    @Mock
    private ServicoService servicoService;

    @Mock
    private InsumoService insumoService;

    @InjectMocks
    private OrdemServicoService ordemServicoService;

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
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServicoDto resultado = ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);

        verify(clienteService).buscarClientePorId(ID_CLIENTE);
        verify(veiculoService).buscarVeiculoPorId(ID_VEICULO);
        verify(ordemServicoRepository).save(ordemServicoCaptor.capture());

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

        List<IniciarAtendimentoRequest.ServicoQuantidade> servicosRequest =
                List.of(new IniciarAtendimentoRequest.ServicoQuantidade(ID_SERVICO, 2));

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_VEICULO)).thenReturn(veiculo);
        when(servicoService.buscarServicoPorId(ID_SERVICO)).thenReturn(servico);
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServicoDto resultado = ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, servicosRequest, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);

        verify(servicoService).buscarServicoPorId(ID_SERVICO);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
        verifyNoInteractions(insumoService);
    }

    @Test
    void deveIniciarAtendimentoComInsumosComSucesso() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        Insumo insumo = criarInsumo();
        OrdemServico ordemSalva = criarOrdemServico(cliente, veiculo);

        List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest =
                List.of(new IniciarAtendimentoRequest.InsumoQuantidade(ID_INSUMO, 3));

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_VEICULO)).thenReturn(veiculo);
        when(insumoService.buscarInsumoPorId(ID_INSUMO)).thenReturn(insumo);
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServicoDto resultado = ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, insumosRequest);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);

        verify(insumoService).buscarInsumoPorId(ID_INSUMO);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
        verifyNoInteractions(servicoService);
    }

    @Test
    void deveIniciarAtendimentoComServicosEInsumosComSucesso() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        Servico servico = criarServico();
        Insumo insumo = criarInsumo();
        OrdemServico ordemSalva = criarOrdemServico(cliente, veiculo);

        List<IniciarAtendimentoRequest.ServicoQuantidade> servicosRequest =
                List.of(new IniciarAtendimentoRequest.ServicoQuantidade(ID_SERVICO, 1));
        List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest =
                List.of(new IniciarAtendimentoRequest.InsumoQuantidade(ID_INSUMO, 2));

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_VEICULO)).thenReturn(veiculo);
        when(servicoService.buscarServicoPorId(ID_SERVICO)).thenReturn(servico);
        when(insumoService.buscarInsumoPorId(ID_INSUMO)).thenReturn(insumo);
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(ordemSalva);

        OrdemServicoDto resultado = ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, servicosRequest, insumosRequest);

        assertThat(resultado).isNotNull();
        verify(servicoService).buscarServicoPorId(ID_SERVICO);
        verify(insumoService).buscarInsumoPorId(ID_INSUMO);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarClienteNotFoundQuandoClienteNaoExistir() {
        when(clienteService.buscarClientePorId(ID_INEXISTENTE))
                .thenThrow(new ClienteNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> ordemServicoService.iniciarAtendimento(ID_INEXISTENTE, ID_VEICULO, RELATO, null, null))
                .isInstanceOf(ClienteNotFound.class);

        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionQuandoVeiculoNaoExistir() {
        Cliente cliente = criarCliente();

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_INEXISTENTE))
                .thenThrow(new VeiculoNaoEncontradoException(ID_INEXISTENTE));

        assertThatThrownBy(() -> ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_INEXISTENTE, RELATO, null, null))
                .isInstanceOf(VeiculoNaoEncontradoException.class);

        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveLancarVeiculoInativoExceptionQuandoVeiculoEstiverInativo() {
        Cliente cliente = criarCliente();

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_VEICULO))
                .thenThrow(new VeiculoInativoException(ID_VEICULO));

        assertThatThrownBy(() -> ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null))
                .isInstanceOf(VeiculoInativoException.class);

        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveLancarInsumoNotFoundQuandoInsumoNaoExistir() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();

        List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest =
                List.of(new IniciarAtendimentoRequest.InsumoQuantidade(ID_INEXISTENTE, 1));

        when(clienteService.buscarClientePorId(ID_CLIENTE)).thenReturn(cliente);
        when(veiculoService.buscarVeiculoPorId(ID_VEICULO)).thenReturn(veiculo);
        when(insumoService.buscarInsumoPorId(ID_INEXISTENTE)).thenThrow(new InsumoNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> ordemServicoService.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, insumosRequest))
                .isInstanceOf(InsumoNotFound.class);

        verify(ordemServicoRepository, never()).save(any());
    }

    // ===================== buscarPorId =====================

    @Test
    void deveBuscarOrdemServicoPorIdComSucesso() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculoAtivo();
        OrdemServico ordemServico = criarOrdemServico(cliente, veiculo);

        when(ordemServicoRepository.findById(UUID_ORDEM)).thenReturn(Optional.of(ordemServico));

        OrdemServicoDto resultado = ordemServicoService.buscarPorId(UUID_ORDEM);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);
        assertThat(resultado.status()).isEqualTo(Status.RECEBIDA);
        assertThat(resultado.cliente()).isNotNull();
        assertThat(resultado.veiculo()).isNotNull();

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

    private OrdemServico criarOrdemServico(Cliente cliente, Veiculo veiculo) {
        return OrdemServico.builder()
                .id(UUID_ORDEM)
                .cliente(cliente)
                .veiculo(veiculo)
                .status(Status.RECEBIDA)
                .build();
    }
}
