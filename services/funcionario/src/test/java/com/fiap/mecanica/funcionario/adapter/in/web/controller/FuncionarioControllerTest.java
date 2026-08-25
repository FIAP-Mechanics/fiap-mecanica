package com.fiap.mecanica.funcionario.adapter.in.web.controller;

import com.fiap.mecanica.funcionario.adapter.in.web.request.AtualizarFuncionarioRequest;
import com.fiap.mecanica.funcionario.adapter.in.web.request.CadastrarFuncionarioRequest;
import com.fiap.mecanica.funcionario.adapter.in.web.response.FuncionarioDto;
import com.fiap.mecanica.funcionario.application.command.AtualizarFuncionarioCommand;
import com.fiap.mecanica.funcionario.application.port.in.FuncionarioUseCase;
import com.fiap.mecanica.funcionario.domain.Funcao;
import com.fiap.mecanica.funcionario.domain.Funcionario;
import com.fiap.mecanica.funcionario.exception.FuncionarioInativoException;
import com.fiap.mecanica.funcionario.exception.FuncionarioJaAtivoException;
import com.fiap.mecanica.funcionario.exception.FuncionarioNotFound;
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
class FuncionarioControllerTest {

    private static final Long ID_EXISTENTE = 1L;
    private static final Long ID_INEXISTENTE = 99L;
    private static final String EMAIL = "joao@email.com";
    private static final String EMAIL_NOVO = "novo@email.com";
    private static final String SENHA = "senha123";
    private static final String SENHA_NOVA = "novaSenha456";
    private static final String NOME = "João Silva";
    private static final String NOME_NOVO = "Carlos Souza";
    private static final Funcao FUNCAO = Funcao.MECANICO;
    private static final Funcao FUNCAO_NOVA = Funcao.ADMIN;

    @Mock
    private FuncionarioUseCase funcionarioUseCase;

    @InjectMocks
    private FuncionarioController controller;

    @Captor
    private ArgumentCaptor<Funcionario> funcionarioCaptor;

    @Test
    void deveRetornarFuncionarioDtoQuandoIdExistir() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(funcionarioUseCase.buscarFuncionarioPorId(ID_EXISTENTE)).thenReturn(funcionario);

        FuncionarioDto resultado = controller.get(ID_EXISTENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.email()).isEqualTo(EMAIL);
        assertThat(resultado.nome()).isEqualTo(NOME);
        assertThat(resultado.funcao()).isEqualTo(FUNCAO);
        verify(funcionarioUseCase).buscarFuncionarioPorId(ID_EXISTENTE);
    }

    @Test
    void deveOmitirSenhaNoRetornoDoGet() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(funcionarioUseCase.buscarFuncionarioPorId(ID_EXISTENTE)).thenReturn(funcionario);

        FuncionarioDto resultado = controller.get(ID_EXISTENTE);

        assertThat(resultado.senha()).isNull();
    }

    @Test
    void deveLancarFuncionarioNotFoundNoGetQuandoIdNaoExistir() {
        when(funcionarioUseCase.buscarFuncionarioPorId(ID_INEXISTENTE))
                .thenThrow(new FuncionarioNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.get(ID_INEXISTENTE))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(funcionarioUseCase).buscarFuncionarioPorId(ID_INEXISTENTE);
    }

    @Test
    void deveLancarFuncionarioInativoExceptionNoGetQuandoFuncionarioEstiverInativo() {
        when(funcionarioUseCase.buscarFuncionarioPorId(ID_EXISTENTE))
                .thenThrow(new FuncionarioInativoException(ID_EXISTENTE));

        assertThatThrownBy(() -> controller.get(ID_EXISTENTE))
                .isInstanceOf(FuncionarioInativoException.class)
                .hasMessage("O Funcionário com ID " + ID_EXISTENTE + " encontra-se inativo");

        verify(funcionarioUseCase).buscarFuncionarioPorId(ID_EXISTENTE);
    }

    @Test
    void deveCadastrarFuncionarioERetornarDtoComSucesso() {
        CadastrarFuncionarioRequest request = criarCadastrarRequest();
        Funcionario funcionarioSalvo = criarFuncionarioAtivo();
        when(funcionarioUseCase.cadastrarFuncionario(any(Funcionario.class))).thenReturn(funcionarioSalvo);

        FuncionarioDto resultado = controller.create(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.email()).isEqualTo(EMAIL);
        assertThat(resultado.nome()).isEqualTo(NOME);
        assertThat(resultado.funcao()).isEqualTo(FUNCAO);
        verify(funcionarioUseCase).cadastrarFuncionario(any(Funcionario.class));
    }

    @Test
    void deveConverterRequestParaEntidadeCorretamenteAoCadastrar() {
        CadastrarFuncionarioRequest request = criarCadastrarRequest();
        Funcionario funcionarioSalvo = criarFuncionarioAtivo();
        when(funcionarioUseCase.cadastrarFuncionario(funcionarioCaptor.capture())).thenReturn(funcionarioSalvo);

        controller.create(request);

        Funcionario capturado = funcionarioCaptor.getValue();
        assertThat(capturado.getEmail()).isEqualTo(EMAIL);
        assertThat(capturado.getNome()).isEqualTo(NOME);
        assertThat(capturado.getSenha()).isEqualTo(SENHA);
        assertThat(capturado.getFuncao()).isEqualTo(FUNCAO);
    }

    @Test
    void deveOmitirSenhaNoRetornoDoCreate() {
        CadastrarFuncionarioRequest request = criarCadastrarRequest();
        when(funcionarioUseCase.cadastrarFuncionario(any(Funcionario.class))).thenReturn(criarFuncionarioAtivo());

        FuncionarioDto resultado = controller.create(request);

        assertThat(resultado.senha()).isNull();
    }

    @Test
    void deveAtualizarFuncionarioERetornarDtoComSucesso() {
        AtualizarFuncionarioRequest request = criarAtualizarRequestCompleto();
        Funcionario funcionarioAtualizado = criarFuncionarioAtualizado();
        when(funcionarioUseCase.atualizarFuncionario(eq(ID_EXISTENTE), any(AtualizarFuncionarioCommand.class)))
                .thenReturn(funcionarioAtualizado);

        FuncionarioDto resultado = controller.update(ID_EXISTENTE, request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.email()).isEqualTo(EMAIL_NOVO);
        assertThat(resultado.nome()).isEqualTo(NOME_NOVO);
        assertThat(resultado.funcao()).isEqualTo(FUNCAO_NOVA);
        verify(funcionarioUseCase).atualizarFuncionario(eq(ID_EXISTENTE), any(AtualizarFuncionarioCommand.class));
    }

    @Test
    void deveConverterRequestParaCommandCorretamenteAoAtualizar() {
        AtualizarFuncionarioRequest request = criarAtualizarRequestCompleto();
        ArgumentCaptor<AtualizarFuncionarioCommand> commandCaptor = ArgumentCaptor.forClass(AtualizarFuncionarioCommand.class);
        when(funcionarioUseCase.atualizarFuncionario(eq(ID_EXISTENTE), commandCaptor.capture()))
                .thenReturn(criarFuncionarioAtualizado());

        controller.update(ID_EXISTENTE, request);

        AtualizarFuncionarioCommand command = commandCaptor.getValue();
        assertThat(command.email()).isEqualTo(EMAIL_NOVO);
        assertThat(command.nome()).isEqualTo(NOME_NOVO);
        assertThat(command.senha()).isEqualTo(SENHA_NOVA);
        assertThat(command.funcao()).isEqualTo(FUNCAO_NOVA);
    }

    @Test
    void deveLancarFuncionarioNotFoundNoUpdateQuandoIdNaoExistir() {
        AtualizarFuncionarioRequest request = criarAtualizarRequestCompleto();
        when(funcionarioUseCase.atualizarFuncionario(eq(ID_INEXISTENTE), any(AtualizarFuncionarioCommand.class)))
                .thenThrow(new FuncionarioNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.update(ID_INEXISTENTE, request))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);
    }

    @Test
    void deveLancarFuncionarioInativoExceptionNoUpdateQuandoFuncionarioEstiverInativo() {
        AtualizarFuncionarioRequest request = criarAtualizarRequestCompleto();
        when(funcionarioUseCase.atualizarFuncionario(eq(ID_EXISTENTE), any(AtualizarFuncionarioCommand.class)))
                .thenThrow(new FuncionarioInativoException(ID_EXISTENTE));

        assertThatThrownBy(() -> controller.update(ID_EXISTENTE, request))
                .isInstanceOf(FuncionarioInativoException.class)
                .hasMessage("O Funcionário com ID " + ID_EXISTENTE + " encontra-se inativo");
    }

    @Test
    void deveOmitirSenhaNoRetornoDoUpdate() {
        AtualizarFuncionarioRequest request = criarAtualizarRequestCompleto();
        when(funcionarioUseCase.atualizarFuncionario(eq(ID_EXISTENTE), any(AtualizarFuncionarioCommand.class)))
                .thenReturn(criarFuncionarioAtualizado());

        FuncionarioDto resultado = controller.update(ID_EXISTENTE, request);

        assertThat(resultado.senha()).isNull();
    }

    @Test
    void deveExcluirFuncionarioLogicamenteERetornarDtoComSucesso() {
        Funcionario funcionarioInativado = criarFuncionarioInativo();
        when(funcionarioUseCase.excluirFuncionario(ID_EXISTENTE)).thenReturn(funcionarioInativado);

        FuncionarioDto resultado = controller.delete(ID_EXISTENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        verify(funcionarioUseCase).excluirFuncionario(ID_EXISTENTE);
    }

    @Test
    void deveLancarFuncionarioNotFoundNoDeleteQuandoIdNaoExistir() {
        when(funcionarioUseCase.excluirFuncionario(ID_INEXISTENTE))
                .thenThrow(new FuncionarioNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.delete(ID_INEXISTENTE))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(funcionarioUseCase).excluirFuncionario(ID_INEXISTENTE);
    }

    @Test
    void deveLancarFuncionarioInativoExceptionNoDeleteQuandoFuncionarioJaEstiverInativo() {
        when(funcionarioUseCase.excluirFuncionario(ID_EXISTENTE))
                .thenThrow(new FuncionarioInativoException(ID_EXISTENTE));

        assertThatThrownBy(() -> controller.delete(ID_EXISTENTE))
                .isInstanceOf(FuncionarioInativoException.class)
                .hasMessage("O Funcionário com ID " + ID_EXISTENTE + " encontra-se inativo");
    }

    @Test
    void deveOmitirSenhaNoRetornoDoDelete() {
        when(funcionarioUseCase.excluirFuncionario(ID_EXISTENTE)).thenReturn(criarFuncionarioInativo());

        FuncionarioDto resultado = controller.delete(ID_EXISTENTE);

        assertThat(resultado.senha()).isNull();
    }

    @Test
    void deveReativarFuncionarioERetornarDtoComSucesso() {
        Funcionario funcionarioReativado = criarFuncionarioAtivo();
        when(funcionarioUseCase.reativarFuncionario(ID_EXISTENTE)).thenReturn(funcionarioReativado);

        FuncionarioDto resultado = controller.reativar(ID_EXISTENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.email()).isEqualTo(EMAIL);
        assertThat(resultado.nome()).isEqualTo(NOME);
        verify(funcionarioUseCase).reativarFuncionario(ID_EXISTENTE);
    }

    @Test
    void deveLancarFuncionarioNotFoundNoReativarQuandoIdNaoExistir() {
        when(funcionarioUseCase.reativarFuncionario(ID_INEXISTENTE))
                .thenThrow(new FuncionarioNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.reativar(ID_INEXISTENTE))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(funcionarioUseCase).reativarFuncionario(ID_INEXISTENTE);
    }

    @Test
    void deveLancarFuncionarioJaAtivoExceptionNoReativarQuandoFuncionarioJaEstiverAtivo() {
        when(funcionarioUseCase.reativarFuncionario(ID_EXISTENTE))
                .thenThrow(new FuncionarioJaAtivoException(ID_EXISTENTE));

        assertThatThrownBy(() -> controller.reativar(ID_EXISTENTE))
                .isInstanceOf(FuncionarioJaAtivoException.class)
                .hasMessage("O Funcionário com id " + ID_EXISTENTE + " já se encontra ativo.");

        verify(funcionarioUseCase).reativarFuncionario(ID_EXISTENTE);
    }

    @Test
    void deveOmitirSenhaNoRetornoDoReativar() {
        when(funcionarioUseCase.reativarFuncionario(ID_EXISTENTE)).thenReturn(criarFuncionarioAtivo());

        FuncionarioDto resultado = controller.reativar(ID_EXISTENTE);

        assertThat(resultado.senha()).isNull();
    }

    @Test
    void deveListarFuncionariosComSucesso() {
        List<Funcionario> funcionarios = List.of(criarFuncionarioAtivo());
        when(funcionarioUseCase.buscarTodos()).thenReturn(funcionarios);

        List<FuncionarioDto> resultado = controller.getList();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().email()).isEqualTo(EMAIL);
        verify(funcionarioUseCase).buscarTodos();
    }

    @Test
    void deveBuscarFuncionarioPorEmailComSucesso() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(funcionarioUseCase.buscarPorEmail(EMAIL)).thenReturn(funcionario);

        FuncionarioDto resultado = controller.getByEmail(EMAIL);

        assertThat(resultado).isNotNull();
        assertThat(resultado.email()).isEqualTo(EMAIL);
        verify(funcionarioUseCase).buscarPorEmail(EMAIL);
    }

    private Funcionario criarFuncionarioAtivo() {
        return Funcionario.builder()
                .id(ID_EXISTENTE)
                .email(EMAIL)
                .senha(SENHA)
                .nome(NOME)
                .funcao(FUNCAO)
                .ativo(true)
                .build();
    }

    private Funcionario criarFuncionarioInativo() {
        return Funcionario.builder()
                .id(ID_EXISTENTE)
                .email(EMAIL)
                .senha(SENHA)
                .nome(NOME)
                .funcao(FUNCAO)
                .ativo(false)
                .build();
    }

    private Funcionario criarFuncionarioAtualizado() {
        return Funcionario.builder()
                .id(ID_EXISTENTE)
                .email(EMAIL_NOVO)
                .senha(SENHA_NOVA)
                .nome(NOME_NOVO)
                .funcao(FUNCAO_NOVA)
                .ativo(true)
                .build();
    }

    private CadastrarFuncionarioRequest criarCadastrarRequest() {
        return CadastrarFuncionarioRequest.builder()
                .email(EMAIL)
                .nome(NOME)
                .senha(SENHA)
                .funcao(FUNCAO)
                .build();
    }

    private AtualizarFuncionarioRequest criarAtualizarRequestCompleto() {
        return new AtualizarFuncionarioRequest(EMAIL_NOVO, NOME_NOVO, SENHA_NOVA, FUNCAO_NOVA);
    }
}
