package com.fiap.mecanica.cliente.adapter.in.web.controller;

import com.fiap.mecanica.cliente.adapter.in.web.request.AtualizarClienteRequest;
import com.fiap.mecanica.cliente.adapter.in.web.request.CadastrarClienteRequest;
import com.fiap.mecanica.cliente.adapter.in.web.response.ClienteDto;
import com.fiap.mecanica.cliente.adapter.in.web.response.EnderecoDto;
import com.fiap.mecanica.cliente.adapter.in.web.response.VeiculoDto;
import com.fiap.mecanica.cliente.application.command.AtualizarClienteCommand;
import com.fiap.mecanica.cliente.application.port.in.ClienteUseCase;
import com.fiap.mecanica.cliente.application.port.in.VinculoVeiculoUseCase;
import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.domain.ClienteVeiculo;
import com.fiap.mecanica.cliente.domain.Endereco;
import com.fiap.mecanica.cliente.exception.ClienteExistente;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private static final Long ID_EXISTENTE = 1L;
    private static final Long ID_INEXISTENTE = 99L;
    private static final Long ID_VEICULO = 10L;
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
    private static final EnderecoDto ENDERECO_DTO = EnderecoDto.builder()
            .cep("30000-000").estado("MG").cidade("Belo Horizonte")
            .bairro("Centro").rua("Rua A").numero("10").build();
    private static final EnderecoDto ENDERECO_NOVO_DTO = EnderecoDto.builder()
            .cep("31000-000").estado("MG").cidade("Contagem")
            .bairro("Industrial").rua("Rua B").numero("20").build();

    @Mock
    private ClienteUseCase clienteUseCase;

    @Mock
    private VinculoVeiculoUseCase vinculoVeiculoUseCase;

    @InjectMocks
    private ClienteController controller;

    @Captor
    private ArgumentCaptor<Cliente> clienteCaptor;

    @Test
    void deveRetornarListaDeClientesDtoComSucesso() {
        List<Cliente> clientes = List.of(criarCliente(), criarCliente());
        when(clienteUseCase.buscarClientes()).thenReturn(clientes);

        List<ClienteDto> resultado = controller.getList();

        assertThat(resultado).hasSize(2);
        verify(clienteUseCase).buscarClientes();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverClientes() {
        when(clienteUseCase.buscarClientes()).thenReturn(List.of());

        List<ClienteDto> resultado = controller.getList();

        assertThat(resultado).isEmpty();
        verify(clienteUseCase).buscarClientes();
    }

    @Test
    void deveRetornarClienteDtoQuandoIdExistir() {
        Cliente cliente = criarCliente();
        when(clienteUseCase.buscarClientePorId(ID_EXISTENTE)).thenReturn(cliente);

        ClienteDto resultado = controller.get(ID_EXISTENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.nome()).isEqualTo(NOME);
        assertThat(resultado.documento()).isEqualTo(DOCUMENTO);
        assertThat(resultado.email()).isEqualTo(EMAIL);
        assertThat(resultado.telefone()).isEqualTo(TELEFONE);
        assertThat(resultado.endereco()).isEqualTo(ENDERECO_DTO);
        verify(clienteUseCase).buscarClientePorId(ID_EXISTENTE);
    }

    @Test
    void deveRetornarClienteDtoQuandoDocumentoExistir() {
        Cliente cliente = criarCliente();
        when(clienteUseCase.buscarClientePorDocumento(DOCUMENTO)).thenReturn(cliente);

        ClienteDto resultado = controller.getByDocumento(DOCUMENTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.nome()).isEqualTo(NOME);
        assertThat(resultado.documento()).isEqualTo(DOCUMENTO);
        verify(clienteUseCase).buscarClientePorDocumento(DOCUMENTO);
    }

    @Test
    void deveLancarClienteNotFoundNoGetByDocumentoQuandoDocumentoNaoExistir() {
        when(clienteUseCase.buscarClientePorDocumento(DOCUMENTO))
                .thenThrow(new ClienteNotFound(DOCUMENTO));

        assertThatThrownBy(() -> controller.getByDocumento(DOCUMENTO))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. Documento: " + DOCUMENTO);

        verify(clienteUseCase).buscarClientePorDocumento(DOCUMENTO);
    }

    @Test
    void deveLancarClienteNotFoundNoGetQuandoIdNaoExistir() {
        when(clienteUseCase.buscarClientePorId(ID_INEXISTENTE))
                .thenThrow(new ClienteNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.get(ID_INEXISTENTE))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);

        verify(clienteUseCase).buscarClientePorId(ID_INEXISTENTE);
    }

    @Test
    void deveCadastrarClienteERetornarDtoComSucesso() {
        CadastrarClienteRequest request = criarCadastrarRequest();
        Cliente clienteSalvo = criarCliente();
        when(clienteUseCase.cadastrarCliente(any(Cliente.class))).thenReturn(clienteSalvo);

        ClienteDto resultado = controller.create(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.nome()).isEqualTo(NOME);
        assertThat(resultado.documento()).isEqualTo(DOCUMENTO);
        assertThat(resultado.email()).isEqualTo(EMAIL);
        assertThat(resultado.telefone()).isEqualTo(TELEFONE);
        verify(clienteUseCase).cadastrarCliente(any(Cliente.class));
    }

    @Test
    void deveConverterRequestParaEntidadeCorretamenteAoCadastrar() {
        CadastrarClienteRequest request = criarCadastrarRequest();
        when(clienteUseCase.cadastrarCliente(clienteCaptor.capture())).thenReturn(criarCliente());

        controller.create(request);

        Cliente capturado = clienteCaptor.getValue();
        assertThat(capturado.getNome()).isEqualTo(NOME);
        assertThat(capturado.getDocumento()).isEqualTo("12345678900");
        assertThat(capturado.getEmail()).isEqualTo(EMAIL);
        assertThat(capturado.getTelefone()).isEqualTo(TELEFONE);
        assertThat(capturado.getEndereco().getCep()).isEqualTo("30000000");
    }

    @Test
    void deveAtualizarClienteERetornarDtoComSucesso() {
        AtualizarClienteRequest request = criarAtualizarRequest();
        Cliente clienteAtualizado = criarClienteAtualizado();
        when(clienteUseCase.atualizarCliente(eq(ID_EXISTENTE), any(AtualizarClienteCommand.class)))
                .thenReturn(clienteAtualizado);

        ClienteDto resultado = controller.update(ID_EXISTENTE, request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.nome()).isEqualTo(NOME_NOVO);
        assertThat(resultado.documento()).isEqualTo(DOCUMENTO_NOVO);
        assertThat(resultado.email()).isEqualTo(EMAIL_NOVO);
        assertThat(resultado.telefone()).isEqualTo(TELEFONE_NOVO);
        assertThat(resultado.endereco()).isEqualTo(ENDERECO_NOVO_DTO);
        verify(clienteUseCase).atualizarCliente(eq(ID_EXISTENTE), any(AtualizarClienteCommand.class));
    }

    @Test
    void deveConverterRequestParaCommandCorretamenteAoAtualizar() {
        AtualizarClienteRequest request = criarAtualizarRequest();
        ArgumentCaptor<AtualizarClienteCommand> commandCaptor = ArgumentCaptor.forClass(AtualizarClienteCommand.class);
        when(clienteUseCase.atualizarCliente(eq(ID_EXISTENTE), commandCaptor.capture()))
                .thenReturn(criarClienteAtualizado());

        controller.update(ID_EXISTENTE, request);

        AtualizarClienteCommand command = commandCaptor.getValue();
        assertThat(command.nome()).isEqualTo(NOME_NOVO);
        assertThat(command.documento()).isEqualTo("98765432100");
        assertThat(command.email()).isEqualTo(EMAIL_NOVO);
        assertThat(command.telefone()).isEqualTo(TELEFONE_NOVO);
        assertThat(command.endereco().getCep()).isEqualTo("31000000");
    }

    @Test
    void deveLancarClienteExistenteAoCadastrarQuandoDocumentoJaExistir() {
        CadastrarClienteRequest request = criarCadastrarRequest();
        when(clienteUseCase.cadastrarCliente(any(Cliente.class)))
                .thenThrow(new ClienteExistente(DOCUMENTO));

        assertThatThrownBy(() -> controller.create(request))
                .isInstanceOf(ClienteExistente.class)
                .hasMessage("Já existe um cliente com o documento: " + DOCUMENTO);
    }

    @Test
    void deveLancarClienteNotFoundNoUpdateQuandoIdNaoExistir() {
        AtualizarClienteRequest request = criarAtualizarRequest();
        when(clienteUseCase.atualizarCliente(eq(ID_INEXISTENTE), any(AtualizarClienteCommand.class)))
                .thenThrow(new ClienteNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.update(ID_INEXISTENTE, request))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);
    }

    @Test
    void deveVincularVeiculoAoClienteComSucesso() {
        controller.vincularClienteVeiculo(ID_EXISTENTE, ID_VEICULO, "ABC1234", "Fiat", "Uno", 2020);

        verify(vinculoVeiculoUseCase).vincularVeiculo(ID_EXISTENTE, ID_VEICULO, "ABC1234", "Fiat", "Uno", 2020);
    }

    @Test
    void deveLancarClienteNotFoundAoVincularQuandoClienteNaoExistir() {
        doThrow(new ClienteNotFound(ID_INEXISTENTE))
                .when(vinculoVeiculoUseCase).vincularVeiculo(ID_INEXISTENTE, ID_VEICULO, null, null, null, null);

        assertThatThrownBy(() -> controller.vincularClienteVeiculo(ID_INEXISTENTE, ID_VEICULO, null, null, null, null))
                .isInstanceOf(ClienteNotFound.class)
                .hasMessage("Cliente não encontrado. ID: " + ID_INEXISTENTE);
    }

    @Test
    void deveLancarVeiculoNaoEncontradoAoVincularQuandoVeiculoNaoExistir() {
        doThrow(new com.fiap.mecanica.cliente.exception.VeiculoNaoEncontradoException(ID_VEICULO))
                .when(vinculoVeiculoUseCase).vincularVeiculo(ID_EXISTENTE, ID_VEICULO, null, null, null, null);

        assertThatThrownBy(() -> controller.vincularClienteVeiculo(ID_EXISTENTE, ID_VEICULO, null, null, null, null))
                .isInstanceOf(com.fiap.mecanica.cliente.exception.VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado. ID: " + ID_VEICULO);
    }

    @Test
    void deveLancarVinculoJaExistenteAoVincularQuandoJaVinculado() {
        doThrow(new VinculoJaExistente(ID_EXISTENTE, ID_VEICULO))
                .when(vinculoVeiculoUseCase).vincularVeiculo(ID_EXISTENTE, ID_VEICULO, null, null, null, null);

        assertThatThrownBy(() -> controller.vincularClienteVeiculo(ID_EXISTENTE, ID_VEICULO, null, null, null, null))
                .isInstanceOf(VinculoJaExistente.class)
                .hasMessage("Veículo " + ID_VEICULO + " já está vinculado ao cliente " + ID_EXISTENTE);
    }

    @Test
    void deveRetornarListaDeVeiculosDoClienteComSucesso() {
        List<ClienteVeiculo> vinculos = List.of(
                ClienteVeiculo.builder().veiculoId(ID_VEICULO).marca("Fiat").modelo("Uno").placa("ABC1234").ano(2020).build()
        );
        when(vinculoVeiculoUseCase.listarVeiculosDoCliente(ID_EXISTENTE)).thenReturn(vinculos);

        List<VeiculoDto> resultado = controller.listarVeiculos(ID_EXISTENTE);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().id()).isEqualTo(ID_VEICULO);
        verify(vinculoVeiculoUseCase).listarVeiculosDoCliente(ID_EXISTENTE);
    }

    @Test
    void deveLancarClienteNotFoundAoListarVeiculosQuandoClienteNaoExistir() {
        when(vinculoVeiculoUseCase.listarVeiculosDoCliente(ID_INEXISTENTE))
                .thenThrow(new ClienteNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.listarVeiculos(ID_INEXISTENTE))
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
        return AtualizarClienteRequest.builder()
                .nome(NOME_NOVO)
                .documento(DOCUMENTO_NOVO)
                .email(EMAIL_NOVO)
                .telefone(TELEFONE_NOVO)
                .endereco(ENDERECO_NOVO)
                .build();
    }
}
