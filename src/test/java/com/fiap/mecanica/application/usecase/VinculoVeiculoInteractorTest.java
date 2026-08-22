package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.port.out.ClienteGateway;
import com.fiap.mecanica.application.port.out.ClienteVeiculoGateway;
import com.fiap.mecanica.application.port.out.VeiculoGateway;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.ClienteVeiculo;
import com.fiap.mecanica.domain.Veiculo;
import com.fiap.mecanica.exception.ClienteNotFound;
import com.fiap.mecanica.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.exception.VinculoJaExistente;
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

    @Mock private ClienteGateway clienteGateway;
    @Mock private VeiculoGateway veiculoGateway;
    @Mock private ClienteVeiculoGateway clienteVeiculoGateway;

    @InjectMocks
    private VinculoVeiculoInteractor interactor;

    @Captor
    private ArgumentCaptor<ClienteVeiculo> vinculoCaptor;

    @Test
    void deveVincularVeiculoAoClienteComSucesso() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculo();
        when(clienteGateway.buscarPorId(ID_CLIENTE)).thenReturn(Optional.of(cliente));
        when(veiculoGateway.buscarPorId(ID_VEICULO)).thenReturn(Optional.of(veiculo));

        interactor.vincularVeiculo(ID_CLIENTE, ID_VEICULO);

        verify(clienteVeiculoGateway).salvar(vinculoCaptor.capture());
        assertThat(vinculoCaptor.getValue().getCliente()).isEqualTo(cliente);
        assertThat(vinculoCaptor.getValue().getVeiculo()).isEqualTo(veiculo);
    }

    @Test
    void deveLancarClienteNotFoundAoVincularQuandoClienteNaoExistir() {
        when(clienteGateway.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.vincularVeiculo(ID_INEXISTENTE, ID_VEICULO))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);

        verify(clienteVeiculoGateway, never()).salvar(any());
    }

    @Test
    void deveLancarVeiculoNaoEncontradoAoVincularQuandoVeiculoNaoExistir() {
        when(clienteGateway.buscarPorId(ID_CLIENTE)).thenReturn(Optional.of(criarCliente()));
        when(veiculoGateway.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.vincularVeiculo(ID_CLIENTE, ID_INEXISTENTE))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado com ID: " + ID_INEXISTENTE);

        verify(clienteVeiculoGateway, never()).salvar(any());
    }

    @Test
    void deveLancarVinculoJaExistenteQuandoVinculoDuplicado() {
        when(clienteGateway.buscarPorId(ID_CLIENTE)).thenReturn(Optional.of(criarCliente()));
        when(veiculoGateway.buscarPorId(ID_VEICULO)).thenReturn(Optional.of(criarVeiculo()));
        when(clienteVeiculoGateway.existeVinculo(ID_CLIENTE, ID_VEICULO)).thenReturn(true);

        assertThatThrownBy(() -> interactor.vincularVeiculo(ID_CLIENTE, ID_VEICULO))
                .isInstanceOf(VinculoJaExistente.class)
                .hasMessage("Veículo " + ID_VEICULO + " já está vinculado ao cliente " + ID_CLIENTE);

        verify(clienteVeiculoGateway, never()).salvar(any());
    }

    @Test
    void deveRetornarListaDeVeiculosDoClienteComSucesso() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculo();
        ClienteVeiculo vinculo = ClienteVeiculo.builder().cliente(cliente).veiculo(veiculo).build();
        when(clienteGateway.buscarPorId(ID_CLIENTE)).thenReturn(Optional.of(cliente));
        when(clienteVeiculoGateway.buscarPorClienteId(ID_CLIENTE)).thenReturn(List.of(vinculo));

        List<Veiculo> resultado = interactor.listarVeiculosDoCliente(ID_CLIENTE);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(ID_VEICULO);
        assertThat(resultado.get(0).getPlaca()).isEqualTo("ABC1234");
    }

    @Test
    void deveRetornarListaVaziaQuandoClienteNaoPossuiVeiculos() {
        when(clienteGateway.buscarPorId(ID_CLIENTE)).thenReturn(Optional.of(criarCliente()));
        when(clienteVeiculoGateway.buscarPorClienteId(ID_CLIENTE)).thenReturn(List.of());

        List<Veiculo> resultado = interactor.listarVeiculosDoCliente(ID_CLIENTE);

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

    private Veiculo criarVeiculo() {
        return Veiculo.builder()
                .id(ID_VEICULO).marca("Fiat").modelo("Uno").placa("ABC1234").ano(2020).build();
    }
}
