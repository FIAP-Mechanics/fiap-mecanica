package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.ClienteVeiculo;
import com.fiap.mecanica.domain.Veiculo;
import com.fiap.mecanica.dto.VeiculoDto;
import com.fiap.mecanica.exception.ClienteNotFound;
import com.fiap.mecanica.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.exception.VinculoJaExistente;
import com.fiap.mecanica.repository.ClienteRepository;
import com.fiap.mecanica.repository.ClienteVeiculoRepository;
import com.fiap.mecanica.repository.VeiculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VinculoVeiculoServiceTest {

    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_VEICULO = 10L;
    private static final Long ID_INEXISTENTE = 99L;

    @Mock private ClienteRepository clienteRepository;
    @Mock private VeiculoRepository veiculoRepository;
    @Mock private ClienteVeiculoRepository clienteVeiculoRepository;

    @InjectMocks
    private VinculoVeiculoService service;

    @Captor
    private ArgumentCaptor<ClienteVeiculo> vinculoCaptor;

    @Test
    void deveVincularVeiculoAoClienteComSucesso() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculo();
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(ID_VEICULO)).thenReturn(Optional.of(veiculo));
        when(clienteVeiculoRepository.existsByClienteIdAndVeiculoId(ID_CLIENTE, ID_VEICULO)).thenReturn(false);

        service.vincularVeiculo(ID_CLIENTE, ID_VEICULO);

        verify(clienteVeiculoRepository).save(vinculoCaptor.capture());
        assertThat(vinculoCaptor.getValue().getCliente()).isEqualTo(cliente);
        assertThat(vinculoCaptor.getValue().getVeiculo()).isEqualTo(veiculo);
    }

    @Test
    void deveLancarClienteNotFoundAoVincularQuandoClienteNaoExistir() {
        when(clienteRepository.findById(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.vincularVeiculo(ID_INEXISTENTE, ID_VEICULO))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);

        verify(clienteVeiculoRepository, never()).save(any());
    }

    @Test
    void deveLancarVeiculoNaoEncontradoAoVincularQuandoVeiculoNaoExistir() {
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(criarCliente()));
        when(veiculoRepository.findById(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.vincularVeiculo(ID_CLIENTE, ID_INEXISTENTE))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado com ID: " + ID_INEXISTENTE);

        verify(clienteVeiculoRepository, never()).save(any());
    }

    @Test
    void deveLancarVinculoJaExistenteQuandoVinculoDuplicado() {
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(criarCliente()));
        when(veiculoRepository.findById(ID_VEICULO)).thenReturn(Optional.of(criarVeiculo()));
        when(clienteVeiculoRepository.existsByClienteIdAndVeiculoId(ID_CLIENTE, ID_VEICULO)).thenReturn(true);

        assertThatThrownBy(() -> service.vincularVeiculo(ID_CLIENTE, ID_VEICULO))
                .isInstanceOf(VinculoJaExistente.class)
                .hasMessage("Veículo " + ID_VEICULO + " já está vinculado ao cliente " + ID_CLIENTE);

        verify(clienteVeiculoRepository, never()).save(any());
    }

    @Test
    void deveRetornarListaDeVeiculosDoClienteComSucesso() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculo();
        ClienteVeiculo vinculo = ClienteVeiculo.builder().cliente(cliente).veiculo(veiculo).build();
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(cliente));
        when(clienteVeiculoRepository.findByClienteId(ID_CLIENTE)).thenReturn(List.of(vinculo));

        List<VeiculoDto> resultado = service.listarVeiculosDoCliente(ID_CLIENTE);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(ID_VEICULO);
        assertThat(resultado.get(0).placa()).isEqualTo("ABC1234");
    }

    @Test
    void deveRetornarListaVaziaQuandoClienteNaoPossuiVeiculos() {
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(criarCliente()));
        when(clienteVeiculoRepository.findByClienteId(ID_CLIENTE)).thenReturn(List.of());

        List<VeiculoDto> resultado = service.listarVeiculosDoCliente(ID_CLIENTE);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveLancarClienteNotFoundAoListarQuandoClienteNaoExistir() {
        when(clienteRepository.findById(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listarVeiculosDoCliente(ID_INEXISTENTE))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);

        verify(clienteVeiculoRepository, never()).findByClienteId(any());
    }

    private Cliente criarCliente() {
        return Cliente.builder().id(ID_CLIENTE).nome("José da Silva").build();
    }

    private Veiculo criarVeiculo() {
        return Veiculo.builder()
                .id(ID_VEICULO).marca("Fiat").modelo("Uno").placa("ABC1234").ano(2020).build();
    }
}
