package com.fiap.mecanica.adapter.in.web.controller;

import com.fiap.mecanica.adapter.in.web.request.AtualizarClienteRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarClienteRequest;
import com.fiap.mecanica.adapter.in.web.response.ClienteDto;
import com.fiap.mecanica.adapter.in.web.response.VeiculoDto;
import com.fiap.mecanica.application.command.AtualizarClienteCommand;
import com.fiap.mecanica.application.port.in.ClienteUseCase;
import com.fiap.mecanica.application.port.in.VinculoVeiculoUseCase;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.Endereco;
import com.fiap.mecanica.domain.Veiculo;
import com.fiap.mecanica.exception.ClienteExistente;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private static final Long ID_EXISTENTE = 1L;
    private static final Long ID_INEXISTENTE = 99L;
    private static final Long ID_VEICULO = 10L;
    private static final Long ID_VEICULO_INEXISTENTE = 98L;
    private static final String NOME = "José da Silva";
    private static final String NOME_NOVO = "Maria Santos";
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
    private static final Endereco ENDERECO_SANITIZADO = Endereco.builder()
            .cep("30000000").estado("MG").cidade("Belo Horizonte")
            .bairro("Centro").rua("Rua A").numero("10").build();
    private static final Endereco ENDERECO_NOVO_SANITIZADO = Endereco.builder()
            .cep("31000000").estado("MG").cidade("Contagem")
            .bairro("Industrial").rua("Rua B").numero("20").build();

    @Mock
    private ClienteUseCase service;

    @Mock
    private VinculoVeiculoUseCase vinculoVeiculoService;

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
        assertThat(resultado.nome()).isEqualTo(NOME);
        assertThat(resultado.documento()).isEqualTo(DOCUMENTO);
        assertThat(resultado.email()).isEqualTo(EMAIL);
        assertThat(resultado.telefone()).isEqualTo(TELEFONE);
        assertThat(resultado.endereco()).isEqualTo(ENDERECO);
        verify(service).buscarClientePorId(ID_EXISTENTE);
    }

    @Test
    void deveRetornarClienteDtoQuandoDocumentoExistir() {
        Cliente cliente = criarCliente();
        when(service.buscarClientePorDocumento(DOCUMENTO)).thenReturn(cliente);

        ClienteDto resultado = controller.getByDocumento(DOCUMENTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.nome()).isEqualTo(NOME);
        assertThat(resultado.documento()).isEqualTo(DOCUMENTO);
        verify(service).buscarClientePorDocumento(DOCUMENTO);
    }

    @Test
    void deveLancarClienteNotFoundNoGetByDocumentoQuandoDocumentoNaoExistir() {
        when(service.buscarClientePorDocumento(DOCUMENTO))
                .thenThrow(new ClienteNotFound(DOCUMENTO));

        assertThatThrownBy(() -> controller.getByDocumento(DOCUMENTO))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. Documento: " + DOCUMENTO);

        verify(service).buscarClientePorDocumento(DOCUMENTO);
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
        assertThat(resultado.nome()).isEqualTo(NOME);
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
        assertThat(capturado.getNome()).isEqualTo(NOME);
        assertThat(capturado.getDocumento()).isEqualTo("12345678900");
        assertThat(capturado.getEmail()).isEqualTo(EMAIL);
        assertThat(capturado.getTelefone()).isEqualTo(TELEFONE);
        assertThat(capturado.getEndereco()).isEqualTo(ENDERECO_SANITIZADO);
    }

    @Test
    void deveAtualizarClienteERetornarDtoComSucesso() {
        AtualizarClienteRequest request = criarAtualizarRequest();
        Cliente clienteAtualizado = criarClienteAtualizado();
        when(service.atualizarCliente(eq(ID_EXISTENTE), any(AtualizarClienteCommand.class)))
                .thenReturn(clienteAtualizado);

        ClienteDto resultado = controller.update(ID_EXISTENTE, request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.nome()).isEqualTo(NOME_NOVO);
        assertThat(resultado.documento()).isEqualTo(DOCUMENTO_NOVO);
        assertThat(resultado.email()).isEqualTo(EMAIL_NOVO);
        assertThat(resultado.telefone()).isEqualTo(TELEFONE_NOVO);
        assertThat(resultado.endereco()).isEqualTo(ENDERECO_NOVO);
        verify(service).atualizarCliente(eq(ID_EXISTENTE), any(AtualizarClienteCommand.class));
    }

    @Test
    void deveConverterRequestParaDtoCorretamenteAoAtualizar() {
        AtualizarClienteRequest request = criarAtualizarRequest();
        ArgumentCaptor<AtualizarClienteCommand> commandCaptor =
                ArgumentCaptor.forClass(AtualizarClienteCommand.class);
        when(service.atualizarCliente(eq(ID_EXISTENTE), commandCaptor.capture()))
                .thenReturn(criarClienteAtualizado());

        controller.update(ID_EXISTENTE, request);

        AtualizarClienteCommand command = commandCaptor.getValue();
        assertThat(command.nome()).isEqualTo(NOME_NOVO);
        assertThat(command.documento()).isEqualTo("98765432100");
        assertThat(command.email()).isEqualTo(EMAIL_NOVO);
        assertThat(command.telefone()).isEqualTo(TELEFONE_NOVO);
        assertThat(command.endereco()).isEqualTo(ENDERECO_NOVO_SANITIZADO);
    }

    @Test
    void deveLancarClienteExistenteAoCadastrarQuandoDocumentoJaExistir() {
        CadastrarClienteRequest request = criarCadastrarRequest();
        when(service.cadastrarCliente(any(Cliente.class)))
                .thenThrow(new ClienteExistente(DOCUMENTO));

        assertThatThrownBy(() -> controller.create(request))
                .isInstanceOf(ClienteExistente.class)
                .hasMessage("Já existe um cliente com o documento: " + DOCUMENTO);
    }

    @Test
    void deveLancarClienteNotFoundNoUpdateQuandoIdNaoExistir() {
        AtualizarClienteRequest request = criarAtualizarRequest();
        when(service.atualizarCliente(eq(ID_INEXISTENTE), any(AtualizarClienteCommand.class)))
                .thenThrow(new ClienteNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.update(ID_INEXISTENTE, request))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);
    }

    private Cliente criarCliente() {
        return Cliente.builder()
                .id(ID_EXISTENTE)
                .nome(NOME)
                .documento(DOCUMENTO)
                .email(EMAIL)
                .telefone(TELEFONE)
                .endereco(ENDERECO)
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

    private CadastrarClienteRequest criarCadastrarRequest() {
        return CadastrarClienteRequest.builder()
                .nome(NOME)
                .documento(DOCUMENTO)
                .email(EMAIL)
                .telefone(TELEFONE)
                .endereco(ENDERECO)
                .build();
    }

    private AtualizarClienteRequest criarAtualizarRequest() {
        return new AtualizarClienteRequest(NOME_NOVO, DOCUMENTO_NOVO, EMAIL_NOVO, TELEFONE_NOVO, ENDERECO_NOVO);
    }

    @Test
    void deveVincularVeiculoAoClienteComSucesso() {
        doNothing().when(vinculoVeiculoService).vincularVeiculo(ID_EXISTENTE, ID_VEICULO);

        controller.vincularClienteVeiculo(ID_EXISTENTE, ID_VEICULO);

        verify(vinculoVeiculoService).vincularVeiculo(ID_EXISTENTE, ID_VEICULO);
    }

    @Test
    void deveLancarClienteNotFoundAoVincularQuandoClienteNaoExistir() {
        doThrow(new ClienteNotFound(ID_INEXISTENTE))
                .when(vinculoVeiculoService).vincularVeiculo(ID_INEXISTENTE, ID_VEICULO);

        assertThatThrownBy(() -> controller.vincularClienteVeiculo(ID_INEXISTENTE, ID_VEICULO))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);
    }

    @Test
    void deveLancarVeiculoNaoEncontradoAoVincularQuandoVeiculoNaoExistir() {
        doThrow(new VeiculoNaoEncontradoException(ID_VEICULO_INEXISTENTE))
                .when(vinculoVeiculoService).vincularVeiculo(ID_EXISTENTE, ID_VEICULO_INEXISTENTE);

        assertThatThrownBy(() -> controller.vincularClienteVeiculo(ID_EXISTENTE, ID_VEICULO_INEXISTENTE))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado com ID: " + ID_VEICULO_INEXISTENTE);
    }

    @Test
    void deveLancarVinculoJaExistenteAoVincularQuandoJaVinculado() {
        doThrow(new VinculoJaExistente(ID_EXISTENTE, ID_VEICULO))
                .when(vinculoVeiculoService).vincularVeiculo(ID_EXISTENTE, ID_VEICULO);

        assertThatThrownBy(() -> controller.vincularClienteVeiculo(ID_EXISTENTE, ID_VEICULO))
                .isInstanceOf(VinculoJaExistente.class)
                .hasMessage("Veículo " + ID_VEICULO + " já está vinculado ao cliente " + ID_EXISTENTE);
    }

    @Test
    void deveRetornarListaDeVeiculosDoClienteComSucesso() {
        List<Veiculo> veiculos = List.of(
                Veiculo.builder().id(ID_VEICULO).marca("Fiat").modelo("Uno").placa("ABC1234").ano(2020).build()
        );
        when(vinculoVeiculoService.listarVeiculosDoCliente(ID_EXISTENTE)).thenReturn(veiculos);

        List<VeiculoDto> resultado = controller.listarVeiculos(ID_EXISTENTE);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(ID_VEICULO);
        verify(vinculoVeiculoService).listarVeiculosDoCliente(ID_EXISTENTE);
    }

    @Test
    void deveLancarClienteNotFoundAoListarVeiculosQuandoClienteNaoExistir() {
        when(vinculoVeiculoService.listarVeiculosDoCliente(ID_INEXISTENTE))
                .thenThrow(new ClienteNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.listarVeiculos(ID_INEXISTENTE))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);
    }
}
