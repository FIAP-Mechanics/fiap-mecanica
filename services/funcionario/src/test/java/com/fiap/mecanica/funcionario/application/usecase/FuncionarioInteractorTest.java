package com.fiap.mecanica.funcionario.application.usecase;

import com.fiap.mecanica.funcionario.application.command.AtualizarFuncionarioCommand;
import com.fiap.mecanica.funcionario.application.port.out.FuncionarioGateway;
import com.fiap.mecanica.funcionario.application.port.out.PasswordEncoderGateway;
import com.fiap.mecanica.funcionario.domain.Funcao;
import com.fiap.mecanica.funcionario.domain.Funcionario;
import com.fiap.mecanica.funcionario.exception.ConflitoException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuncionarioInteractorTest {

    private static final Long ID_EXISTENTE = 1L;
    private static final Long ID_INEXISTENTE = 99L;
    private static final String EMAIL_ORIGINAL = "original@email.com";
    private static final String EMAIL_NOVO = "novo@email.com";
    private static final String SENHA_ORIGINAL = "senha123";
    private static final String SENHA_NOVA = "novaSenha456";
    private static final String NOME_ORIGINAL = "João Silva";
    private static final String NOME_NOVO = "Carlos Souza";
    private static final Funcao FUNCAO_ORIGINAL = Funcao.MECANICO;
    private static final Funcao FUNCAO_NOVA = Funcao.ADMIN;
    private static final String SENHA_ORIGINAL_CODIFICADA = "senha-original-codificada";
    private static final String SENHA_NOVA_CODIFICADA = "senha-nova-codificada";

    @Mock
    private FuncionarioGateway funcionarioGateway;
    @Mock
    private PasswordEncoderGateway passwordEncoderGateway;

    @InjectMocks
    private FuncionarioInteractor interactor;

    @Captor
    private ArgumentCaptor<Funcionario> funcionarioCaptor;

    @Test
    void deveCadastrarFuncionarioComSucesso() {
        Funcionario funcionario = criarFuncionarioParaCadastro();
        when(funcionarioGateway.existePorEmail(EMAIL_ORIGINAL)).thenReturn(false);
        when(passwordEncoderGateway.encode(SENHA_ORIGINAL)).thenReturn(SENHA_ORIGINAL_CODIFICADA);
        when(funcionarioGateway.salvar(funcionario)).thenReturn(funcionario);

        Funcionario resultado = interactor.cadastrarFuncionario(funcionario);

        assertThat(resultado).isEqualTo(funcionario);
        verify(funcionarioGateway).salvar(funcionario);
    }

    @Test
    void deveLancarConflitoExceptionAoCadastrarQuandoEmailJaExistir() {
        Funcionario funcionario = criarFuncionarioParaCadastro();
        when(funcionarioGateway.existePorEmail(EMAIL_ORIGINAL)).thenReturn(true);

        assertThatThrownBy(() -> interactor.cadastrarFuncionario(funcionario))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("Já existe um funcionário cadastrado com o e-mail: " + EMAIL_ORIGINAL);

        verify(funcionarioGateway, never()).salvar(any());
    }

    @Test
    void devePersistirFuncionarioExatamenteComoRecebido() {
        Funcionario funcionario = criarFuncionarioParaCadastro();
        when(funcionarioGateway.existePorEmail(EMAIL_ORIGINAL)).thenReturn(false);
        when(passwordEncoderGateway.encode(SENHA_ORIGINAL)).thenReturn(SENHA_ORIGINAL_CODIFICADA);
        when(funcionarioGateway.salvar(any())).thenReturn(funcionario);

        interactor.cadastrarFuncionario(funcionario);

        verify(funcionarioGateway).salvar(funcionarioCaptor.capture());
        assertThat(funcionarioCaptor.getValue().getEmail()).isEqualTo(EMAIL_ORIGINAL);
        assertThat(funcionarioCaptor.getValue().getSenha()).isEqualTo(SENHA_ORIGINAL_CODIFICADA);
        assertThat(funcionarioCaptor.getValue().getNome()).isEqualTo(NOME_ORIGINAL);
        assertThat(funcionarioCaptor.getValue().getFuncao()).isEqualTo(FUNCAO_ORIGINAL);
    }

    @Test
    void deveRetornarFuncionarioQuandoIdExistirEFuncionarioEstiverAtivo() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(funcionarioGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));

        Funcionario resultado = interactor.buscarFuncionarioPorId(ID_EXISTENTE);

        assertThat(resultado).isEqualTo(funcionario);
        verify(funcionarioGateway).buscarPorId(ID_EXISTENTE);
    }

    @Test
    void deveLancarFuncionarioNotFoundQuandoIdNaoExistir() {
        when(funcionarioGateway.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.buscarFuncionarioPorId(ID_INEXISTENTE))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(funcionarioGateway).buscarPorId(ID_INEXISTENTE);
    }

    @Test
    void deveLancarFuncionarioInativoExceptionQuandoFuncionarioEstiverInativo() {
        Funcionario funcionarioInativo = criarFuncionarioInativo();
        when(funcionarioGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionarioInativo));

        assertThatThrownBy(() -> interactor.buscarFuncionarioPorId(ID_EXISTENTE))
                .isInstanceOf(FuncionarioInativoException.class)
                .hasMessage("O Funcionário com ID " + ID_EXISTENTE + " encontra-se inativo");

        verify(funcionarioGateway).buscarPorId(ID_EXISTENTE);
    }

    @Test
    void deveAtualizarTodosOsCamposQuandoTodosForemInformados() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(funcionarioGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(funcionarioGateway.salvar(any())).thenReturn(funcionario);
        when(passwordEncoderGateway.encode(SENHA_NOVA)).thenReturn(SENHA_NOVA_CODIFICADA);

        AtualizarFuncionarioCommand command = criarCommandCompleto();
        interactor.atualizarFuncionario(ID_EXISTENTE, command);

        verify(funcionarioGateway).salvar(funcionarioCaptor.capture());
        Funcionario salvo = funcionarioCaptor.getValue();
        assertThat(salvo.getEmail()).isEqualTo(EMAIL_NOVO);
        assertThat(salvo.getSenha()).isEqualTo(SENHA_NOVA_CODIFICADA);
        assertThat(salvo.getNome()).isEqualTo(NOME_NOVO);
        assertThat(salvo.getFuncao()).isEqualTo(FUNCAO_NOVA);
    }

    @Test
    void deveAtualizarSomenteCamposInformadosQuandoCommandForParcial() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(funcionarioGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(funcionarioGateway.salvar(any())).thenReturn(funcionario);

        AtualizarFuncionarioCommand command = AtualizarFuncionarioCommand.builder().email(EMAIL_NOVO).build();
        interactor.atualizarFuncionario(ID_EXISTENTE, command);

        verify(funcionarioGateway).salvar(funcionarioCaptor.capture());
        Funcionario salvo = funcionarioCaptor.getValue();
        assertThat(salvo.getEmail()).isEqualTo(EMAIL_NOVO);
        assertThat(salvo.getSenha()).isEqualTo(SENHA_ORIGINAL_CODIFICADA);
        assertThat(salvo.getNome()).isEqualTo(NOME_ORIGINAL);
        assertThat(salvo.getFuncao()).isEqualTo(FUNCAO_ORIGINAL);
    }

    @Test
    void deveManterTodosOsCamposQuandoCommandForVazio() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(funcionarioGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(funcionarioGateway.salvar(any())).thenReturn(funcionario);

        AtualizarFuncionarioCommand command = AtualizarFuncionarioCommand.builder().build();
        interactor.atualizarFuncionario(ID_EXISTENTE, command);

        verify(funcionarioGateway).salvar(funcionarioCaptor.capture());
        Funcionario salvo = funcionarioCaptor.getValue();
        assertThat(salvo.getEmail()).isEqualTo(EMAIL_ORIGINAL);
        assertThat(salvo.getSenha()).isEqualTo(SENHA_ORIGINAL_CODIFICADA);
        assertThat(salvo.getNome()).isEqualTo(NOME_ORIGINAL);
        assertThat(salvo.getFuncao()).isEqualTo(FUNCAO_ORIGINAL);
    }

    @Test
    void deveRetornarFuncionarioAtualizadoAoSalvar() {
        Funcionario funcionario = criarFuncionarioAtivo();
        Funcionario funcionarioAtualizado = criarFuncionarioAtualizado();
        when(funcionarioGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(funcionarioGateway.salvar(any())).thenReturn(funcionarioAtualizado);

        AtualizarFuncionarioCommand command = criarCommandCompleto();
        Funcionario resultado = interactor.atualizarFuncionario(ID_EXISTENTE, command);

        assertThat(resultado).isEqualTo(funcionarioAtualizado);
    }

    @Test
    void deveLancarFuncionarioNotFoundAoAtualizarQuandoIdNaoExistir() {
        when(funcionarioGateway.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        AtualizarFuncionarioCommand command = criarCommandCompleto();

        assertThatThrownBy(() -> interactor.atualizarFuncionario(ID_INEXISTENTE, command))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(funcionarioGateway, never()).salvar(any());
    }

    @Test
    void deveLancarFuncionarioInativoExceptionAoAtualizarQuandoFuncionarioEstiverInativo() {
        Funcionario funcionarioInativo = criarFuncionarioInativo();
        when(funcionarioGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionarioInativo));

        AtualizarFuncionarioCommand command = criarCommandCompleto();

        assertThatThrownBy(() -> interactor.atualizarFuncionario(ID_EXISTENTE, command))
                .isInstanceOf(FuncionarioInativoException.class)
                .hasMessage("O Funcionário com ID " + ID_EXISTENTE + " encontra-se inativo");

        verify(funcionarioGateway, never()).salvar(any());
    }

    @Test
    void deveRealizarExclusaoLogicaComSucesso() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(funcionarioGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(funcionarioGateway.salvar(any())).thenReturn(funcionario);

        interactor.excluirFuncionario(ID_EXISTENTE);

        verify(funcionarioGateway).salvar(funcionarioCaptor.capture());
        assertThat(funcionarioCaptor.getValue().isAtivo()).isFalse();
    }

    @Test
    void deveRetornarFuncionarioInativoAposExclusaoLogica() {
        Funcionario funcionario = criarFuncionarioAtivo();
        Funcionario funcionarioInativado = criarFuncionarioInativo();
        when(funcionarioGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(funcionarioGateway.salvar(any())).thenReturn(funcionarioInativado);

        Funcionario resultado = interactor.excluirFuncionario(ID_EXISTENTE);

        assertThat(resultado.isAtivo()).isFalse();
    }

    @Test
    void deveLancarFuncionarioNotFoundAoExcluirQuandoIdNaoExistir() {
        when(funcionarioGateway.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.excluirFuncionario(ID_INEXISTENTE))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(funcionarioGateway, never()).salvar(any());
    }

    @Test
    void deveLancarFuncionarioInativoExceptionAoExcluirQuandoFuncionarioJaEstiverInativo() {
        Funcionario funcionarioInativo = criarFuncionarioInativo();
        when(funcionarioGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionarioInativo));

        assertThatThrownBy(() -> interactor.excluirFuncionario(ID_EXISTENTE))
                .isInstanceOf(FuncionarioInativoException.class)
                .hasMessage("O Funcionário com ID " + ID_EXISTENTE + " encontra-se inativo");

        verify(funcionarioGateway, never()).salvar(any());
    }

    @Test
    void deveReativarFuncionarioInativoComSucesso() {
        Funcionario funcionarioInativo = criarFuncionarioInativo();
        when(funcionarioGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionarioInativo));
        when(funcionarioGateway.salvar(any())).thenReturn(funcionarioInativo);

        interactor.reativarFuncionario(ID_EXISTENTE);

        verify(funcionarioGateway).salvar(funcionarioCaptor.capture());
        assertThat(funcionarioCaptor.getValue().isAtivo()).isTrue();
    }

    @Test
    void deveRetornarFuncionarioAtivoAposReativacao() {
        Funcionario funcionarioInativo = criarFuncionarioInativo();
        Funcionario funcionarioReativado = criarFuncionarioAtivo();
        when(funcionarioGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionarioInativo));
        when(funcionarioGateway.salvar(any())).thenReturn(funcionarioReativado);

        Funcionario resultado = interactor.reativarFuncionario(ID_EXISTENTE);

        assertThat(resultado.isAtivo()).isTrue();
    }

    @Test
    void deveLancarFuncionarioNotFoundAoReativarQuandoIdNaoExistir() {
        when(funcionarioGateway.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.reativarFuncionario(ID_INEXISTENTE))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(funcionarioGateway, never()).salvar(any());
    }

    @Test
    void deveLancarFuncionarioJaAtivoExceptionAoReativarQuandoFuncionarioJaEstiverAtivo() {
        Funcionario funcionarioAtivo = criarFuncionarioAtivo();
        when(funcionarioGateway.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionarioAtivo));

        assertThatThrownBy(() -> interactor.reativarFuncionario(ID_EXISTENTE))
                .isInstanceOf(FuncionarioJaAtivoException.class)
                .hasMessage("O Funcionário com id " + ID_EXISTENTE + " já se encontra ativo.");

        verify(funcionarioGateway, never()).salvar(any());
    }

    @Test
    void deveBuscarTodosComSucesso() {
        List<Funcionario> funcionarios = List.of(criarFuncionarioAtivo());
        when(funcionarioGateway.buscarTodos()).thenReturn(funcionarios);

        List<Funcionario> resultado = interactor.buscarTodos();

        assertThat(resultado).hasSize(1);
        verify(funcionarioGateway).buscarTodos();
    }

    @Test
    void deveBuscarPorEmailComSucesso() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(funcionarioGateway.buscarPorEmail(EMAIL_ORIGINAL)).thenReturn(Optional.of(funcionario));

        Funcionario resultado = interactor.buscarPorEmail(EMAIL_ORIGINAL);

        assertThat(resultado).isEqualTo(funcionario);
        verify(funcionarioGateway).buscarPorEmail(EMAIL_ORIGINAL);
    }

    @Test
    void deveLancarFuncionarioNotFoundAoBuscarPorEmailQuandoEmailNaoExistir() {
        when(funcionarioGateway.buscarPorEmail(EMAIL_NOVO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.buscarPorEmail(EMAIL_NOVO))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com e-mail: " + EMAIL_NOVO);

        verify(funcionarioGateway).buscarPorEmail(EMAIL_NOVO);
    }

    @Test
    void deveLancarFuncionarioInativoExceptionAoBuscarPorEmailQuandoFuncionarioEstiverInativo() {
        Funcionario funcionarioInativo = criarFuncionarioInativo();
        when(funcionarioGateway.buscarPorEmail(EMAIL_ORIGINAL)).thenReturn(Optional.of(funcionarioInativo));

        assertThatThrownBy(() -> interactor.buscarPorEmail(EMAIL_ORIGINAL))
                .isInstanceOf(FuncionarioInativoException.class)
                .hasMessage("O Funcionário com ID " + ID_EXISTENTE + " encontra-se inativo");

        verify(funcionarioGateway).buscarPorEmail(EMAIL_ORIGINAL);
    }

    private Funcionario criarFuncionarioAtivo() {
        return Funcionario.builder()
                .id(ID_EXISTENTE)
                .email(EMAIL_ORIGINAL)
                .senha(SENHA_ORIGINAL_CODIFICADA)
                .nome(NOME_ORIGINAL)
                .funcao(FUNCAO_ORIGINAL)
                .ativo(true)
                .build();
    }

    private Funcionario criarFuncionarioInativo() {
        return Funcionario.builder()
                .id(ID_EXISTENTE)
                .email(EMAIL_ORIGINAL)
                .senha(SENHA_ORIGINAL_CODIFICADA)
                .nome(NOME_ORIGINAL)
                .funcao(FUNCAO_ORIGINAL)
                .ativo(false)
                .build();
    }

    private Funcionario criarFuncionarioParaCadastro() {
        return Funcionario.builder()
                .email(EMAIL_ORIGINAL)
                .senha(SENHA_ORIGINAL)
                .nome(NOME_ORIGINAL)
                .funcao(FUNCAO_ORIGINAL)
                .ativo(true)
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

    private AtualizarFuncionarioCommand criarCommandCompleto() {
        return AtualizarFuncionarioCommand.builder()
                .email(EMAIL_NOVO)
                .senha(SENHA_NOVA)
                .nome(NOME_NOVO)
                .funcao(FUNCAO_NOVA)
                .build();
    }
}
