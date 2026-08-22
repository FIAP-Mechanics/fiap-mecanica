package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AtualizarClienteCommand;
import com.fiap.mecanica.application.port.out.ClienteGateway;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.Endereco;
import com.fiap.mecanica.exception.ClienteNotFound;
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
    private ClienteGateway repository;

    @InjectMocks
    private ClienteInteractor service;

    @Captor
    private ArgumentCaptor<Cliente> clienteCaptor;

    @Test
    void deveCadastrarClienteComSucesso() {
        Cliente cliente = criarCliente();
        when(repository.salvar(cliente)).thenReturn(cliente);

        Cliente resultado = service.cadastrarCliente(cliente);

        assertThat(resultado).isEqualTo(cliente);
        verify(repository).salvar(cliente);
    }

    @Test
    void devePersistirClienteExatamenteComoRecebido() {
        Cliente cliente = criarCliente();
        when(repository.salvar(any())).thenReturn(cliente);

        service.cadastrarCliente(cliente);

        verify(repository).salvar(clienteCaptor.capture());
        assertThat(clienteCaptor.getValue()).isEqualTo(cliente);
    }

    @Test
    void deveRetornarListaDeClientesComSucesso() {
        List<Cliente> clientes = List.of(criarCliente(), criarCliente());
        when(repository.buscarTodos()).thenReturn(clientes);

        List<Cliente> resultado = service.buscarClientes();

        assertThat(resultado).hasSize(2);
        verify(repository).buscarTodos();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverClientes() {
        when(repository.buscarTodos()).thenReturn(List.of());

        List<Cliente> resultado = service.buscarClientes();

        assertThat(resultado).isEmpty();
        verify(repository).buscarTodos();
    }

    @Test
    void deveRetornarClienteQuandoIdExistir() {
        Cliente cliente = criarCliente();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));

        Cliente resultado = service.buscarClientePorId(ID_EXISTENTE);

        assertThat(resultado).isEqualTo(cliente);
        verify(repository).buscarPorId(ID_EXISTENTE);
    }

    @Test
    void deveRetornarClienteQuandoDocumentoExistir() {
        Cliente cliente = criarCliente();
        when(repository.buscarPorDocumento(DOCUMENTO_ORIGINAL)).thenReturn(Optional.of(cliente));

        Cliente resultado = service.buscarClientePorDocumento(DOCUMENTO_ORIGINAL_FORMATADO);

        assertThat(resultado).isEqualTo(cliente);
        verify(repository).buscarPorDocumento(DOCUMENTO_ORIGINAL);
    }

    @Test
    void deveLancarClienteNotFoundQuandoDocumentoNaoExistir() {
        when(repository.buscarPorDocumento(DOCUMENTO_ORIGINAL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarClientePorDocumento(DOCUMENTO_ORIGINAL_FORMATADO))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. Documento: " + DOCUMENTO_ORIGINAL);

        verify(repository).buscarPorDocumento(DOCUMENTO_ORIGINAL);
    }

    @Test
    void deveLancarClienteNotFoundQuandoIdNaoExistir() {
        when(repository.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarClientePorId(ID_INEXISTENTE))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);

        verify(repository).buscarPorId(ID_INEXISTENTE);
    }

    @Test
    void deveAtualizarTodosOsCamposQuandoTodosForemInformados() {
        Cliente cliente = criarCliente();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(repository.salvar(any())).thenReturn(cliente);

        service.atualizarCliente(ID_EXISTENTE, criarDtoCompleto());

        verify(repository).salvar(clienteCaptor.capture());
        Cliente salvo = clienteCaptor.getValue();
        assertThat(salvo.getNome()).isEqualTo(NOME_NOVO);
        assertThat(salvo.getDocumento()).isEqualTo(DOCUMENTO_NOVO);
        assertThat(salvo.getEmail()).isEqualTo(EMAIL_NOVO);
        assertThat(salvo.getTelefone()).isEqualTo(TELEFONE_NOVO);
        assertThat(salvo.getEndereco()).isEqualTo(ENDERECO_NOVO);
    }

    @Test
    void deveAtualizarSomenteCamposInformadosQuandoDtoForParcial() {
        Cliente cliente = criarCliente();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(repository.salvar(any())).thenReturn(cliente);

        AtualizarClienteCommand command =
                new AtualizarClienteCommand(null, DOCUMENTO_NOVO, null, null, null);
        service.atualizarCliente(ID_EXISTENTE, command);

        verify(repository).salvar(clienteCaptor.capture());
        Cliente salvo = clienteCaptor.getValue();
        assertThat(salvo.getNome()).isEqualTo(NOME_ORIGINAL);
        assertThat(salvo.getDocumento()).isEqualTo(DOCUMENTO_NOVO);
        assertThat(salvo.getEmail()).isEqualTo(EMAIL_ORIGINAL);
        assertThat(salvo.getTelefone()).isEqualTo(TELEFONE_ORIGINAL);
        assertThat(salvo.getEndereco()).isEqualTo(ENDERECO_ORIGINAL);
    }

    @Test
    void deveManterTodosOsCamposQuandoDtoForVazio() {
        Cliente cliente = criarCliente();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(repository.salvar(any())).thenReturn(cliente);

        AtualizarClienteCommand command =
                new AtualizarClienteCommand(null, null, null, null, null);
        service.atualizarCliente(ID_EXISTENTE, command);

        verify(repository).salvar(clienteCaptor.capture());
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
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(repository.salvar(any())).thenReturn(clienteAtualizado);

        Cliente resultado = service.atualizarCliente(ID_EXISTENTE, criarDtoCompleto());

        assertThat(resultado).isEqualTo(clienteAtualizado);
    }

    @Test
    void deveLancarClienteNotFoundAoAtualizarQuandoIdNaoExistir() {
        when(repository.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizarCliente(ID_INEXISTENTE, criarDtoCompleto()))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveLancarClienteExistenteAoCadastrarQuandoDocumentoJaExistir() {
        Cliente cliente = criarCliente();
        when(repository.existePorDocumento(DOCUMENTO_ORIGINAL)).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrarCliente(cliente))
                .isInstanceOf(com.fiap.mecanica.exception.ClienteExistente.class)
                .hasMessage("Já existe um cliente com o documento: " + DOCUMENTO_ORIGINAL);

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveNaoAtualizarCampoQuandoValorNovoForIgualAoValorAtual() {
        Cliente cliente = criarCliente();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(repository.salvar(any())).thenReturn(cliente);

        AtualizarClienteCommand command = new AtualizarClienteCommand(
                NOME_ORIGINAL,
                DOCUMENTO_ORIGINAL,
                EMAIL_ORIGINAL,
                TELEFONE_ORIGINAL,
                ENDERECO_ORIGINAL);
        service.atualizarCliente(ID_EXISTENTE, command);

        verify(repository).salvar(clienteCaptor.capture());
        Cliente salvo = clienteCaptor.getValue();
        assertThat(salvo.getNome()).isEqualTo(NOME_ORIGINAL);
        assertThat(salvo.getDocumento()).isEqualTo(DOCUMENTO_ORIGINAL);
        assertThat(salvo.getEmail()).isEqualTo(EMAIL_ORIGINAL);
        assertThat(salvo.getTelefone()).isEqualTo(TELEFONE_ORIGINAL);
        assertThat(salvo.getEndereco()).isEqualTo(ENDERECO_ORIGINAL);
    }

    @Test
    void deveAtualizarSomenteNomeQuandoApenasNomeForInformado() {
        Cliente cliente = criarCliente();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(repository.salvar(any())).thenReturn(cliente);

        service.atualizarCliente(
                ID_EXISTENTE,
                new AtualizarClienteCommand(NOME_NOVO, null, null, null, null));

        verify(repository).salvar(clienteCaptor.capture());
        assertThat(clienteCaptor.getValue().getNome()).isEqualTo(NOME_NOVO);
        assertThat(clienteCaptor.getValue().getDocumento()).isEqualTo(DOCUMENTO_ORIGINAL);
    }

    @Test
    void deveAtualizarSomenteDocumentoQuandoApenasDocumentoForInformado() {
        Cliente cliente = criarCliente();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(repository.salvar(any())).thenReturn(cliente);

        service.atualizarCliente(
                ID_EXISTENTE,
                new AtualizarClienteCommand(null, DOCUMENTO_NOVO, null, null, null));

        verify(repository).salvar(clienteCaptor.capture());
        assertThat(clienteCaptor.getValue().getDocumento()).isEqualTo(DOCUMENTO_NOVO);
    }

    @Test
    void deveAtualizarSomenteEmailQuandoApenasEmailForInformado() {
        Cliente cliente = criarCliente();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(repository.salvar(any())).thenReturn(cliente);

        service.atualizarCliente(
                ID_EXISTENTE,
                new AtualizarClienteCommand(null, null, EMAIL_NOVO, null, null));

        verify(repository).salvar(clienteCaptor.capture());
        assertThat(clienteCaptor.getValue().getEmail()).isEqualTo(EMAIL_NOVO);
    }

    @Test
    void deveAtualizarSomenteTelefoneQuandoApenasTelefoneForInformado() {
        Cliente cliente = criarCliente();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(repository.salvar(any())).thenReturn(cliente);

        service.atualizarCliente(
                ID_EXISTENTE,
                new AtualizarClienteCommand(null, null, null, TELEFONE_NOVO, null));

        verify(repository).salvar(clienteCaptor.capture());
        assertThat(clienteCaptor.getValue().getTelefone()).isEqualTo(TELEFONE_NOVO);
    }

    @Test
    void deveAtualizarSomenteEnderecoQuandoApenasEnderecoForInformado() {
        Cliente cliente = criarCliente();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(repository.salvar(any())).thenReturn(cliente);

        service.atualizarCliente(
                ID_EXISTENTE,
                new AtualizarClienteCommand(null, null, null, null, ENDERECO_NOVO));

        verify(repository).salvar(clienteCaptor.capture());
        assertThat(clienteCaptor.getValue().getEndereco()).isEqualTo(ENDERECO_NOVO);
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

    private AtualizarClienteCommand criarDtoCompleto() {
        return new AtualizarClienteCommand(
                NOME_NOVO,
                DOCUMENTO_NOVO,
                EMAIL_NOVO,
                TELEFONE_NOVO,
                ENDERECO_NOVO);
    }
}
