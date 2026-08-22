package com.fiap.mecanica.cliente.service;

import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.domain.ClienteVeiculo;
import com.fiap.mecanica.cliente.dto.VeiculoDto;
import com.fiap.mecanica.cliente.exception.ClienteNotFound;
import com.fiap.mecanica.cliente.exception.VinculoJaExistente;
import com.fiap.mecanica.cliente.repository.ClienteRepository;
import com.fiap.mecanica.cliente.repository.ClienteVeiculoRepository;
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
    private static final String PLACA = "ABC1234";
    private static final String MARCA = "Fiat";
    private static final String MODELO = "Uno";
    private static final Integer ANO = 2020;

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ClienteVeiculoRepository clienteVeiculoRepository;

    @InjectMocks
    private VinculoVeiculoService service;

    @Captor
    private ArgumentCaptor<ClienteVeiculo> vinculoCaptor;

    @Test
    void deveVincularVeiculoAoClienteComSucesso() {
        Cliente cliente = criarCliente();
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(cliente));
        when(clienteVeiculoRepository.existsByClienteIdAndVeiculoId(ID_CLIENTE, ID_VEICULO)).thenReturn(false);

        service.vincularVeiculo(ID_CLIENTE, ID_VEICULO, PLACA, MARCA, MODELO, ANO);

        verify(clienteVeiculoRepository).save(vinculoCaptor.capture());
        assertThat(vinculoCaptor.getValue().getCliente()).isEqualTo(cliente);
        assertThat(vinculoCaptor.getValue().getVeiculoId()).isEqualTo(ID_VEICULO);
        assertThat(vinculoCaptor.getValue().getPlaca()).isEqualTo(PLACA);
        assertThat(vinculoCaptor.getValue().getMarca()).isEqualTo(MARCA);
        assertThat(vinculoCaptor.getValue().getModelo()).isEqualTo(MODELO);
        assertThat(vinculoCaptor.getValue().getAno()).isEqualTo(ANO);
    }

    @Test
    void deveLancarClienteNotFoundAoVincularQuandoClienteNaoExistir() {
        when(clienteRepository.findById(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.vincularVeiculo(ID_INEXISTENTE, ID_VEICULO, PLACA, MARCA, MODELO, ANO))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);

        verify(clienteVeiculoRepository, never()).save(any());
    }

    @Test
    void deveLancarVinculoJaExistenteQuandoVinculoDuplicado() {
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(criarCliente()));
        when(clienteVeiculoRepository.existsByClienteIdAndVeiculoId(ID_CLIENTE, ID_VEICULO)).thenReturn(true);

        assertThatThrownBy(() -> service.vincularVeiculo(ID_CLIENTE, ID_VEICULO, PLACA, MARCA, MODELO, ANO))
                .isInstanceOf(VinculoJaExistente.class)
                .hasMessage("Veículo " + ID_VEICULO + " já está vinculado ao cliente " + ID_CLIENTE);

        verify(clienteVeiculoRepository, never()).save(any());
    }

    @Test
    void deveRetornarListaDeVeiculosDoClienteComSucesso() {
        Cliente cliente = criarCliente();
        ClienteVeiculo vinculo = ClienteVeiculo.builder()
                .cliente(cliente)
                .veiculoId(ID_VEICULO)
                .placa(PLACA)
                .marca(MARCA)
                .modelo(MODELO)
                .ano(ANO)
                .build();
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(cliente));
        when(clienteVeiculoRepository.findByClienteId(ID_CLIENTE)).thenReturn(List.of(vinculo));

        List<VeiculoDto> resultado = service.listarVeiculosDoCliente(ID_CLIENTE);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().id()).isEqualTo(ID_VEICULO);
        assertThat(resultado.getFirst().placa()).isEqualTo(PLACA);
        assertThat(resultado.getFirst().marca()).isEqualTo(MARCA);
        assertThat(resultado.getFirst().modelo()).isEqualTo(MODELO);
        assertThat(resultado.getFirst().ano()).isEqualTo(ANO);
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
}
