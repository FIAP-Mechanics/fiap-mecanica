package com.fiap.mecanica.cliente.application.usecase;

import com.fiap.mecanica.cliente.application.port.out.ClienteGateway;
import com.fiap.mecanica.cliente.application.port.out.ClienteVeiculoGateway;
import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.domain.ClienteVeiculo;
import com.fiap.mecanica.cliente.exception.ClienteNotFound;
import com.fiap.mecanica.cliente.exception.VinculoJaExistente;
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
class VinculoVeiculoInteractorTest {

    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_VEICULO = 10L;
    private static final Long ID_INEXISTENTE = 99L;
    private static final String PLACA = "ABC1234";
    private static final String MARCA = "Fiat";
    private static final String MODELO = "Uno";
    private static final Integer ANO = 2020;

    @Mock
    private ClienteGateway clienteGateway;
    @Mock
    private ClienteVeiculoGateway clienteVeiculoGateway;

    @InjectMocks
    private VinculoVeiculoInteractor interactor;

    @Captor
    private ArgumentCaptor<ClienteVeiculo> vinculoCaptor;

    @Test
    void deveVincularVeiculoAoClienteComSucesso() {
        Cliente cliente = criarCliente();
        when(clienteGateway.buscarPorId(ID_CLIENTE)).thenReturn(Optional.of(cliente));
        when(clienteVeiculoGateway.existeVinculo(ID_CLIENTE, ID_VEICULO)).thenReturn(false);

        interactor.vincularVeiculo(ID_CLIENTE, ID_VEICULO, PLACA, MARCA, MODELO, ANO);

        verify(clienteVeiculoGateway).salvar(vinculoCaptor.capture());
        assertThat(vinculoCaptor.getValue().getCliente()).isEqualTo(cliente);
        assertThat(vinculoCaptor.getValue().getVeiculoId()).isEqualTo(ID_VEICULO);
        assertThat(vinculoCaptor.getValue().getPlaca()).isEqualTo(PLACA);
        assertThat(vinculoCaptor.getValue().getMarca()).isEqualTo(MARCA);
        assertThat(vinculoCaptor.getValue().getModelo()).isEqualTo(MODELO);
        assertThat(vinculoCaptor.getValue().getAno()).isEqualTo(ANO);
    }

    @Test
    void deveLancarClienteNotFoundAoVincularQuandoClienteNaoExistir() {
        when(clienteGateway.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.vincularVeiculo(ID_INEXISTENTE, ID_VEICULO, PLACA, MARCA, MODELO, ANO))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);

        verify(clienteVeiculoGateway, never()).salvar(any());
    }

    @Test
    void deveLancarVinculoJaExistenteQuandoVinculoDuplicado() {
        when(clienteGateway.buscarPorId(ID_CLIENTE)).thenReturn(Optional.of(criarCliente()));
        when(clienteVeiculoGateway.existeVinculo(ID_CLIENTE, ID_VEICULO)).thenReturn(true);

        assertThatThrownBy(() -> interactor.vincularVeiculo(ID_CLIENTE, ID_VEICULO, PLACA, MARCA, MODELO, ANO))
                .isInstanceOf(VinculoJaExistente.class)
                .hasMessage("Veículo " + ID_VEICULO + " já está vinculado ao cliente " + ID_CLIENTE);

        verify(clienteVeiculoGateway, never()).salvar(any());
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
        when(clienteGateway.buscarPorId(ID_CLIENTE)).thenReturn(Optional.of(cliente));
        when(clienteVeiculoGateway.buscarPorClienteId(ID_CLIENTE)).thenReturn(List.of(vinculo));

        List<ClienteVeiculo> resultado = interactor.listarVeiculosDoCliente(ID_CLIENTE);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getVeiculoId()).isEqualTo(ID_VEICULO);
        assertThat(resultado.getFirst().getPlaca()).isEqualTo(PLACA);
        assertThat(resultado.getFirst().getMarca()).isEqualTo(MARCA);
        assertThat(resultado.getFirst().getModelo()).isEqualTo(MODELO);
        assertThat(resultado.getFirst().getAno()).isEqualTo(ANO);
    }

    @Test
    void deveRetornarListaVaziaQuandoClienteNaoPossuiVeiculos() {
        when(clienteGateway.buscarPorId(ID_CLIENTE)).thenReturn(Optional.of(criarCliente()));
        when(clienteVeiculoGateway.buscarPorClienteId(ID_CLIENTE)).thenReturn(List.of());

        List<ClienteVeiculo> resultado = interactor.listarVeiculosDoCliente(ID_CLIENTE);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveLancarClienteNotFoundAoListarQuandoClienteNaoExistir() {
        when(clienteGateway.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.listarVeiculosDoCliente(ID_INEXISTENTE))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);

        verify(clienteVeiculoGateway, never()).buscarPorClienteId(any());
    }

    private Cliente criarCliente() {
        return Cliente.builder().id(ID_CLIENTE).nome("José da Silva").build();
    }
}
