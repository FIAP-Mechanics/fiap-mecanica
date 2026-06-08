package com.fiap.mecanica.controller;

import com.fiap.mecanica.controller.request.AtualizarClienteRequest;
import com.fiap.mecanica.controller.request.CadastrarClienteRequest;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.Endereco;
import com.fiap.mecanica.dto.ClienteDto;
import com.fiap.mecanica.exception.ClienteNotFound;
import com.fiap.mecanica.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private static final Long ID_EXISTENTE = 1L;
    private static final Long ID_INEXISTENTE = 99L;
    private static final String DOCUMENTO = "123.456.789-00";
    private static final String DOCUMENTO_NOVO = "987.654.321-00";
    private static final String EMAIL = "cliente@email.com";
    private static final String EMAIL_NOVO = "novo@email.com";
    private static final String TELEFONE = "31998000000";
    private static final String TELEFONE_NOVO = "31999111111";
    private static final Endereco ENDERECO = Endereco.builder()
            .cep("30000-000").estado("MG").cidade("Belo Horizonte")
            .bairro("Centro").rua("Rua A").numero("10").build();
    private static final Endereco ENDERECO_NOVO = Endereco.builder()
            .cep("31000-000").estado("MG").cidade("Contagem")
            .bairro("Industrial").rua("Rua B").numero("20").build();

    @Mock
    private ClienteService service;

    @InjectMocks
    private ClienteController controller;

    @Captor
    private ArgumentCaptor<Cliente> clienteCaptor;

    @Test
    void deveRetornarListaDeClientesDtoComSucesso() {
        List<Cliente> clientes = List.of(criarCliente(), criarCliente());
        when(service.buscarClientes()).thenReturn(clientes);

        List<ClienteDto> resultado = controller.getList();

        assertThat(resultado).hasSize(2);
        verify(service).buscarClientes();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverClientes() {
        when(service.buscarClientes()).thenReturn(List.of());

        List<ClienteDto> resultado = controller.getList();

        assertThat(resultado).isEmpty();
        verify(service).buscarClientes();
    }

    @Test
    void deveRetornarClienteDtoQuandoIdExistir() {
        Cliente cliente = criarCliente();
        when(service.buscarClientePorId(ID_EXISTENTE)).thenReturn(cliente);

        ClienteDto resultado = controller.get(ID_EXISTENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.documento()).isEqualTo(DOCUMENTO);
        assertThat(resultado.email()).isEqualTo(EMAIL);
        assertThat(resultado.telefone()).isEqualTo(TELEFONE);
        assertThat(resultado.endereco()).isEqualTo(ENDERECO);
        verify(service).buscarClientePorId(ID_EXISTENTE);
    }

    @Test
    void deveLancarClienteNotFoundNoGetQuandoIdNaoExistir() {
        when(service.buscarClientePorId(ID_INEXISTENTE))
                .thenThrow(new ClienteNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.get(ID_INEXISTENTE))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);

        verify(service).buscarClientePorId(ID_INEXISTENTE);
    }

    @Test
    void deveCadastrarClienteERetornarDtoComSucesso() {
        CadastrarClienteRequest request = criarCadastrarRequest();
        Cliente clienteSalvo = criarCliente();
        when(service.cadastrarCliente(any(Cliente.class))).thenReturn(clienteSalvo);

        ClienteDto resultado = controller.create(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.documento()).isEqualTo(DOCUMENTO);
        assertThat(resultado.email()).isEqualTo(EMAIL);
        assertThat(resultado.telefone()).isEqualTo(TELEFONE);
        verify(service).cadastrarCliente(any(Cliente.class));
    }

    @Test
    void deveConverterRequestParaEntidadeCorretamenteAoCadastrar() {
        CadastrarClienteRequest request = criarCadastrarRequest();
        when(service.cadastrarCliente(clienteCaptor.capture())).thenReturn(criarCliente());

        controller.create(request);

        Cliente capturado = clienteCaptor.getValue();
        assertThat(capturado.getDocumento()).isEqualTo(DOCUMENTO);
        assertThat(capturado.getEmail()).isEqualTo(EMAIL);
        assertThat(capturado.getTelefone()).isEqualTo(TELEFONE);
        assertThat(capturado.getEndereco()).isEqualTo(ENDERECO);
    }

    @Test
    void deveAtualizarClienteERetornarDtoComSucesso() {
        AtualizarClienteRequest request = criarAtualizarRequest();
        Cliente clienteAtualizado = criarClienteAtualizado();
        when(service.atualizarCliente(eq(ID_EXISTENTE), any(ClienteDto.class)))
                .thenReturn(clienteAtualizado);

        ClienteDto resultado = controller.update(ID_EXISTENTE, request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.documento()).isEqualTo(DOCUMENTO_NOVO);
        assertThat(resultado.email()).isEqualTo(EMAIL_NOVO);
        assertThat(resultado.telefone()).isEqualTo(TELEFONE_NOVO);
        assertThat(resultado.endereco()).isEqualTo(ENDERECO_NOVO);
        verify(service).atualizarCliente(eq(ID_EXISTENTE), any(ClienteDto.class));
    }

    @Test
    void deveConverterRequestParaDtoCorretamenteAoAtualizar() {
        AtualizarClienteRequest request = criarAtualizarRequest();
        ArgumentCaptor<ClienteDto> dtoCaptor = ArgumentCaptor.forClass(ClienteDto.class);
        when(service.atualizarCliente(eq(ID_EXISTENTE), dtoCaptor.capture()))
                .thenReturn(criarClienteAtualizado());

        controller.update(ID_EXISTENTE, request);

        ClienteDto dto = dtoCaptor.getValue();
        assertThat(dto.documento()).isEqualTo(DOCUMENTO_NOVO);
        assertThat(dto.email()).isEqualTo(EMAIL_NOVO);
        assertThat(dto.telefone()).isEqualTo(TELEFONE_NOVO);
        assertThat(dto.endereco()).isEqualTo(ENDERECO_NOVO);
    }

    @Test
    void deveLancarClienteNotFoundNoUpdateQuandoIdNaoExistir() {
        AtualizarClienteRequest request = criarAtualizarRequest();
        when(service.atualizarCliente(eq(ID_INEXISTENTE), any(ClienteDto.class)))
                .thenThrow(new ClienteNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.update(ID_INEXISTENTE, request))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);
    }

    private Cliente criarCliente() {
        return Cliente.builder()
                .id(ID_EXISTENTE)
                .documento(DOCUMENTO)
                .email(EMAIL)
                .telefone(TELEFONE)
                .endereco(ENDERECO)
                .build();
    }

    private Cliente criarClienteAtualizado() {
        return Cliente.builder()
                .id(ID_EXISTENTE)
                .documento(DOCUMENTO_NOVO)
                .email(EMAIL_NOVO)
                .telefone(TELEFONE_NOVO)
                .endereco(ENDERECO_NOVO)
                .build();
    }

    private CadastrarClienteRequest criarCadastrarRequest() {
        return CadastrarClienteRequest.builder()
                .documento(DOCUMENTO)
                .email(EMAIL)
                .telefone(TELEFONE)
                .endereco(ENDERECO)
                .build();
    }

    private AtualizarClienteRequest criarAtualizarRequest() {
        return new AtualizarClienteRequest(DOCUMENTO_NOVO, EMAIL_NOVO, TELEFONE_NOVO, ENDERECO_NOVO);
    }
}
