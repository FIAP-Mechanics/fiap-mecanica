package com.fiap.mecanica.cliente.application.usecase;

import com.fiap.mecanica.cliente.application.command.AtualizarClienteCommand;
import com.fiap.mecanica.cliente.application.port.out.ClienteGateway;
import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.domain.Endereco;
import com.fiap.mecanica.cliente.exception.ClienteExistente;
import com.fiap.mecanica.cliente.exception.ClienteNotFound;
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
class ClienteInteractorTest {

    private static final Long ID_EXISTENTE = 1L;
    private static final Long ID_INEXISTENTE = 99L;
    private static final String NOME_ORIGINAL = "José da Silva";
    private static final String NOME_NOVO = "Maria Santos";
    private static final String DOCUMENTO_ORIGINAL = "12345678900";
    private static final String DOCUMENTO_ORIGINAL_FORMATADO = "123.456.789-00";
    private static final String DOCUMENTO_NOVO = "98765432100";
    private static final String EMAIL_ORIGINAL = "cliente@email.com";
    private static final String EMAIL_NOVO = "novo@email.com";
    private static final String TELEFONE_ORIGINAL = "31998000000";
    private static final String TELEFONE_NOVO = "31999111111";
    private static final Endereco ENDERECO_ORIGINAL = Endereco.builder()
            .cep("30000000").estado("MG").cidade("Belo Horizonte")
            .bairro("Centro").rua("Rua A").numero("10").build();
    private static final Endereco ENDERECO_NOVO = Endereco.builder()
            .cep("31000000").estado("MG").cidade("Contagem")
            .bairro("Industrial").rua("Rua B").numero("20").build();

    @Mock
    private ClienteGateway clienteGateway;

    @InjectMocks
    private ClienteInteractor interactor;

    @Captor
    private ArgumentCaptor<Cliente> clienteCaptor;

    @Test
    void deveCadastrarClienteComSucesso() {
        Cliente cliente = criarCliente();
        when(clienteGateway.salvar(any(Cliente.class))).thenReturn(cliente);
        when(clienteGateway.existePorDocumento(DOCUMENTO_ORIGINAL)).thenReturn(false);

        Cliente resultado = interactor.cadastrarCliente(cliente);

        assertThat(resultado).isEqualTo(cliente);
        verify(clienteGateway).salvar(cliente);
    }

    @Test
    void devePersistirClienteComDocumentoNormalizado() {
        Cliente cliente = criarClienteComDocumentoFormatado();
        when(clienteGateway.salvar(any())).thenReturn(cliente);
        when(clienteGateway.existePorDocumento(DOCUMENTO_ORIGINAL)).thenReturn(false);

        interactor.cadastrarCliente(cliente);

        verify(clienteGateway).salvar(clienteCaptor.capture());
        assertThat(clienteCaptor.getValue().getDocumento()).isEqualTo(DOCUMENTO_ORIGINAL);
    }

    @Test
    void deveRetornarListaDeClientesComSucesso() {
        List<Cliente> clientes = List.of(criarCliente(), criarCliente());
        when(clienteGateway.buscarTodos()).thenReturn(clientes);

        List<Cliente> resultado = interactor.buscarClientes();

        assertThat(resultado).hasSize(2);
        verify(clienteGateway).buscarTodos();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverClientes() {
        when(clienteGateway.buscarTodos()).thenReturn(List.of());

        List<Cliente> resultado = interactor.buscarClientes();

        assertThat(resultado).isEmpty();
        verify(clienteGateway).buscarTodos();
    }

    @Test
    void deveRetornarClienteQuandoIdExistir() {
        Cliente cliente = criarCliente();
        when(clienteGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));

        Cliente resultado = interactor.buscarClientePorId(ID_EXISTENTE);

        assertThat(resultado).isEqualTo(cliente);
        verify(clienteGateway).buscarPorId(ID_EXISTENTE);
    }

    @Test
    void deveRetornarClienteQuandoDocumentoExistir() {
        Cliente cliente = criarCliente();
        when(clienteGateway.buscarPorDocumento(DOCUMENTO_ORIGINAL)).thenReturn(Optional.of(cliente));

        Cliente resultado = interactor.buscarClientePorDocumento(DOCUMENTO_ORIGINAL_FORMATADO);

        assertThat(resultado).isEqualTo(cliente);
        verify(clienteGateway).buscarPorDocumento(DOCUMENTO_ORIGINAL);
    }

    @Test
    void deveLancarClienteNotFoundQuandoDocumentoNaoExistir() {
        when(clienteGateway.buscarPorDocumento(DOCUMENTO_ORIGINAL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.buscarClientePorDocumento(DOCUMENTO_ORIGINAL_FORMATADO))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. Documento: " + DOCUMENTO_ORIGINAL);

        verify(clienteGateway).buscarPorDocumento(DOCUMENTO_ORIGINAL);
    }

    @Test
    void deveLancarClienteNotFoundQuandoIdNaoExistir() {
        when(clienteGateway.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.buscarClientePorId(ID_INEXISTENTE))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);

        verify(clienteGateway).buscarPorId(ID_INEXISTENTE);
    }

    @Test
    void deveAtualizarTodosOsCamposQuandoTodosForemInformados() {
        Cliente cliente = criarCliente();
        when(clienteGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(clienteGateway.salvar(any())).thenReturn(cliente);

        interactor.atualizarCliente(ID_EXISTENTE, criarCommandCompleto());

        verify(clienteGateway).salvar(clienteCaptor.capture());
        Cliente salvo = clienteCaptor.getValue();
        assertThat(salvo.getNome()).isEqualTo(NOME_NOVO);
        assertThat(salvo.getDocumento()).isEqualTo(DOCUMENTO_NOVO);
        assertThat(salvo.getEmail()).isEqualTo(EMAIL_NOVO);
        assertThat(salvo.getTelefone()).isEqualTo(TELEFONE_NOVO);
        assertThat(salvo.getEndereco()).isEqualTo(ENDERECO_NOVO);
    }

    @Test
    void deveAtualizarSomenteCamposInformadosQuandoCommandForParcial() {
        Cliente cliente = criarCliente();
        when(clienteGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(clienteGateway.salvar(any())).thenReturn(cliente);

        AtualizarClienteCommand command = AtualizarClienteCommand.builder().documento(DOCUMENTO_NOVO).build();
        interactor.atualizarCliente(ID_EXISTENTE, command);

        verify(clienteGateway).salvar(clienteCaptor.capture());
        Cliente salvo = clienteCaptor.getValue();
        assertThat(salvo.getNome()).isEqualTo(NOME_ORIGINAL);
        assertThat(salvo.getDocumento()).isEqualTo(DOCUMENTO_NOVO);
        assertThat(salvo.getEmail()).isEqualTo(EMAIL_ORIGINAL);
        assertThat(salvo.getTelefone()).isEqualTo(TELEFONE_ORIGINAL);
        assertThat(salvo.getEndereco()).isEqualTo(ENDERECO_ORIGINAL);
    }

    @Test
    void deveManterTodosOsCamposQuandoCommandForVazio() {
        Cliente cliente = criarCliente();
        when(clienteGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(clienteGateway.salvar(any())).thenReturn(cliente);

        AtualizarClienteCommand command = AtualizarClienteCommand.builder().build();
        interactor.atualizarCliente(ID_EXISTENTE, command);

        verify(clienteGateway).salvar(clienteCaptor.capture());
        Cliente salvo = clienteCaptor.getValue();
        assertThat(salvo.getNome()).isEqualTo(NOME_ORIGINAL);
        assertThat(salvo.getDocumento()).isEqualTo(DOCUMENTO_ORIGINAL);
        assertThat(salvo.getEmail()).isEqualTo(EMAIL_ORIGINAL);
        assertThat(salvo.getTelefone()).isEqualTo(TELEFONE_ORIGINAL);
        assertThat(salvo.getEndereco()).isEqualTo(ENDERECO_ORIGINAL);
    }

    @Test
    void deveRetornarClienteAtualizadoAoSalvar() {
        Cliente cliente = criarCliente();
        Cliente clienteAtualizado = criarClienteAtualizado();
        when(clienteGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(clienteGateway.salvar(any())).thenReturn(clienteAtualizado);

        Cliente resultado = interactor.atualizarCliente(ID_EXISTENTE, criarCommandCompleto());

        assertThat(resultado).isEqualTo(clienteAtualizado);
    }

    @Test
    void deveLancarClienteNotFoundAoAtualizarQuandoIdNaoExistir() {
        when(clienteGateway.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.atualizarCliente(ID_INEXISTENTE, criarCommandCompleto()))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);

        verify(clienteGateway, never()).salvar(any());
    }

    @Test
    void deveLancarClienteExistenteAoCadastrarQuandoDocumentoJaExistir() {
        Cliente cliente = criarCliente();
        when(clienteGateway.existePorDocumento(DOCUMENTO_ORIGINAL)).thenReturn(true);

        assertThatThrownBy(() -> interactor.cadastrarCliente(cliente))
                .isInstanceOf(ClienteExistente.class)
                .hasMessage("Já existe um cliente com o documento: " + DOCUMENTO_ORIGINAL);

        verify(clienteGateway, never()).salvar(any());
    }

    @Test
    void deveNaoAtualizarCampoQuandoValorNovoForIgualAoValorAtual() {
        Cliente cliente = criarCliente();
        when(clienteGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(clienteGateway.salvar(any())).thenReturn(cliente);

        AtualizarClienteCommand command = AtualizarClienteCommand.builder()
                .nome(NOME_ORIGINAL)
                .documento(DOCUMENTO_ORIGINAL)
                .email(EMAIL_ORIGINAL)
                .telefone(TELEFONE_ORIGINAL)
                .endereco(ENDERECO_ORIGINAL)
                .build();
        interactor.atualizarCliente(ID_EXISTENTE, command);

        verify(clienteGateway).salvar(clienteCaptor.capture());
        Cliente salvo = clienteCaptor.getValue();
        assertThat(salvo.getNome()).isEqualTo(NOME_ORIGINAL);
        assertThat(salvo.getDocumento()).isEqualTo(DOCUMENTO_ORIGINAL);
        assertThat(salvo.getEmail()).isEqualTo(EMAIL_ORIGINAL);
        assertThat(salvo.getTelefone()).isEqualTo(TELEFONE_ORIGINAL);
        assertThat(salvo.getEndereco()).isEqualTo(ENDERECO_ORIGINAL);
    }

    private Cliente criarCliente() {
        return Cliente.builder()
                .id(ID_EXISTENTE)
                .nome(NOME_ORIGINAL)
                .documento(DOCUMENTO_ORIGINAL)
                .email(EMAIL_ORIGINAL)
                .telefone(TELEFONE_ORIGINAL)
                .endereco(ENDERECO_ORIGINAL)
                .build();
    }

    private Cliente criarClienteComDocumentoFormatado() {
        return Cliente.builder()
                .id(ID_EXISTENTE)
                .nome(NOME_ORIGINAL)
                .documento(DOCUMENTO_ORIGINAL_FORMATADO)
                .email(EMAIL_ORIGINAL)
                .telefone(TELEFONE_ORIGINAL)
                .endereco(ENDERECO_ORIGINAL)
                .build();
    }

    private Cliente criarClienteAtualizado() {
        return Cliente.builder()
                .id(ID_EXISTENTE)
                .nome(NOME_NOVO)
                .documento(DOCUMENTO_NOVO)
                .email(EMAIL_NOVO)
                .telefone(TELEFONE_NOVO)
                .endereco(ENDERECO_NOVO)
                .build();
    }

    private AtualizarClienteCommand criarCommandCompleto() {
        return AtualizarClienteCommand.builder()
                .nome(NOME_NOVO)
                .documento(DOCUMENTO_NOVO)
                .email(EMAIL_NOVO)
                .telefone(TELEFONE_NOVO)
                .endereco(ENDERECO_NOVO)
                .build();
    }
}
