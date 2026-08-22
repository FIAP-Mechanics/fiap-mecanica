package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AtualizarFuncionarioCommand;
import com.fiap.mecanica.application.port.out.CodificadorSenhaGateway;
import com.fiap.mecanica.application.port.out.FuncionarioGateway;
import com.fiap.mecanica.domain.Funcao;
import com.fiap.mecanica.domain.Funcionario;
import com.fiap.mecanica.exception.FuncionarioInativoException;
import com.fiap.mecanica.exception.FuncionarioJaAtivoException;
import com.fiap.mecanica.exception.FuncionarioNotFound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private FuncionarioGateway repository;
    @Mock
    private CodificadorSenhaGateway codificadorSenha;

    @InjectMocks
    private FuncionarioInteractor service;

    @Captor
    private ArgumentCaptor<Funcionario> funcionarioCaptor;

    @Test
    void deveCadastrarFuncionarioComSucesso() {
        Funcionario funcionario = criarFuncionarioParaCadastro();
        when(codificadorSenha.codificar(SENHA_ORIGINAL)).thenReturn(SENHA_ORIGINAL_CODIFICADA);
        when(repository.salvar(funcionario)).thenReturn(funcionario);

        Funcionario resultado = service.cadastrarFuncionario(funcionario);

        assertThat(resultado).isEqualTo(funcionario);
        verify(repository).salvar(funcionario);
    }

    @Test
    void devePersistirFuncionarioExatamenteComoRecebido() {
        Funcionario funcionario = criarFuncionarioParaCadastro();
        when(codificadorSenha.codificar(SENHA_ORIGINAL)).thenReturn(SENHA_ORIGINAL_CODIFICADA);
        when(repository.salvar(any())).thenReturn(funcionario);

        service.cadastrarFuncionario(funcionario);

        verify(repository).salvar(funcionarioCaptor.capture());
        assertThat(funcionarioCaptor.getValue().getEmail()).isEqualTo(EMAIL_ORIGINAL);
        assertThat(funcionarioCaptor.getValue().getSenha()).isEqualTo(SENHA_ORIGINAL_CODIFICADA);
        assertThat(funcionarioCaptor.getValue().getNome()).isEqualTo(NOME_ORIGINAL);
        assertThat(funcionarioCaptor.getValue().getFuncao()).isEqualTo(FUNCAO_ORIGINAL);
    }


    @Test
    void deveRetornarFuncionarioQuandoIdExistirEFuncionarioEstiverAtivo() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));

        Funcionario resultado = service.buscarFuncionarioPorId(ID_EXISTENTE);

        assertThat(resultado).isEqualTo(funcionario);
        verify(repository).buscarPorId(ID_EXISTENTE);
    }

    @Test
    void deveLancarFuncionarioNotFoundQuandoIdNaoExistir() {
        when(repository.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarFuncionarioPorId(ID_INEXISTENTE))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(repository).buscarPorId(ID_INEXISTENTE);
    }

    @Test
    void deveLancarFuncionarioInativoExceptionQuandoFuncionarioEstiverInativo() {
        Funcionario funcionarioInativo = criarFuncionarioInativo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionarioInativo));

        assertThatThrownBy(() -> service.buscarFuncionarioPorId(ID_EXISTENTE))
                .isInstanceOf(FuncionarioInativoException.class)
                .hasMessage("O Funcionário com ID " + ID_EXISTENTE + " encontra-se inativo");

        verify(repository).buscarPorId(ID_EXISTENTE);
    }

    @Test
    void deveAtualizarTodosOsCamposQuandoTodosForemInformados() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(repository.salvar(any())).thenReturn(funcionario);
        when(codificadorSenha.codificar(SENHA_NOVA)).thenReturn(SENHA_NOVA_CODIFICADA);

        AtualizarFuncionarioCommand command = criarDtoCompleto();
        service.atualizarFuncionario(ID_EXISTENTE, command);

        verify(repository).salvar(funcionarioCaptor.capture());
        Funcionario salvo = funcionarioCaptor.getValue();
        assertThat(salvo.getEmail()).isEqualTo(EMAIL_NOVO);
        assertThat(salvo.getSenha()).isEqualTo(SENHA_NOVA_CODIFICADA);
        assertThat(salvo.getNome()).isEqualTo(NOME_NOVO);
        assertThat(salvo.getFuncao()).isEqualTo(FUNCAO_NOVA);
    }

    @Test
    void deveAtualizarSomenteCamposInformadosQuandoDtoForParcial() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(repository.salvar(any())).thenReturn(funcionario);

        AtualizarFuncionarioCommand command =
                new AtualizarFuncionarioCommand(EMAIL_NOVO, null, null, null);
        service.atualizarFuncionario(ID_EXISTENTE, command);

        verify(repository).salvar(funcionarioCaptor.capture());
        Funcionario salvo = funcionarioCaptor.getValue();
        assertThat(salvo.getEmail()).isEqualTo(EMAIL_NOVO);
        assertThat(salvo.getSenha()).isEqualTo(SENHA_ORIGINAL_CODIFICADA);
        assertThat(salvo.getNome()).isEqualTo(NOME_ORIGINAL);
        assertThat(salvo.getFuncao()).isEqualTo(FUNCAO_ORIGINAL);
    }

    @Test
    void deveManterTodosOsCamposQuandoDtoForVazio() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(repository.salvar(any())).thenReturn(funcionario);

        AtualizarFuncionarioCommand command =
                new AtualizarFuncionarioCommand(null, null, null, null);
        service.atualizarFuncionario(ID_EXISTENTE, command);

        verify(repository).salvar(funcionarioCaptor.capture());
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
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(repository.salvar(any())).thenReturn(funcionarioAtualizado);

        AtualizarFuncionarioCommand command = criarDtoCompleto();
        Funcionario resultado = service.atualizarFuncionario(ID_EXISTENTE, command);

        assertThat(resultado).isEqualTo(funcionarioAtualizado);
    }

    @Test
    void deveLancarFuncionarioNotFoundAoAtualizarQuandoIdNaoExistir() {
        when(repository.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        AtualizarFuncionarioCommand command = criarDtoCompleto();

        assertThatThrownBy(() -> service.atualizarFuncionario(ID_INEXISTENTE, command))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveLancarFuncionarioInativoExceptionAoAtualizarQuandoFuncionarioEstiverInativo() {
        Funcionario funcionarioInativo = criarFuncionarioInativo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionarioInativo));

        AtualizarFuncionarioCommand command = criarDtoCompleto();

        assertThatThrownBy(() -> service.atualizarFuncionario(ID_EXISTENTE, command))
                .isInstanceOf(FuncionarioInativoException.class)
                .hasMessage("O Funcionário com ID " + ID_EXISTENTE + " encontra-se inativo");

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveAtualizarSomenteEmailQuandoApenaEmailForInformado() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(repository.salvar(any())).thenReturn(funcionario);

        AtualizarFuncionarioCommand command =
                new AtualizarFuncionarioCommand(EMAIL_NOVO, null, null, null);
        service.atualizarFuncionario(ID_EXISTENTE, command);

        verify(repository).salvar(funcionarioCaptor.capture());
        assertThat(funcionarioCaptor.getValue().getEmail()).isEqualTo(EMAIL_NOVO);
    }

    @Test
    void deveAtualizarSomenteSenhaQuandoApenaSenhaForInformada() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(repository.salvar(any())).thenReturn(funcionario);
        when(codificadorSenha.codificar(SENHA_NOVA)).thenReturn(SENHA_NOVA_CODIFICADA);

        AtualizarFuncionarioCommand command =
                new AtualizarFuncionarioCommand(null, SENHA_NOVA, null, null);
        service.atualizarFuncionario(ID_EXISTENTE, command);

        verify(repository).salvar(funcionarioCaptor.capture());
        assertThat(funcionarioCaptor.getValue().getSenha()).isEqualTo(SENHA_NOVA_CODIFICADA);
    }

    @Test
    void deveAtualizarSomenteNomeQuandoApenasNomeForInformado() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(repository.salvar(any())).thenReturn(funcionario);

        AtualizarFuncionarioCommand command =
                new AtualizarFuncionarioCommand(null, null, NOME_NOVO, null);
        service.atualizarFuncionario(ID_EXISTENTE, command);

        verify(repository).salvar(funcionarioCaptor.capture());
        assertThat(funcionarioCaptor.getValue().getNome()).isEqualTo(NOME_NOVO);
    }

    @Test
    void deveAtualizarSomenteFuncaoQuandoApenasAFuncaoForInformada() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(repository.salvar(any())).thenReturn(funcionario);

        AtualizarFuncionarioCommand command =
                new AtualizarFuncionarioCommand(null, null, null, FUNCAO_NOVA);
        service.atualizarFuncionario(ID_EXISTENTE, command);

        verify(repository).salvar(funcionarioCaptor.capture());
        assertThat(funcionarioCaptor.getValue().getFuncao()).isEqualTo(FUNCAO_NOVA);
    }

    @Test
    void deveRealizarExclusaoLogicaComSucesso() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(repository.salvar(any())).thenReturn(funcionario);

        service.excluirFuncionario(ID_EXISTENTE);

        verify(repository).salvar(funcionarioCaptor.capture());
        assertThat(funcionarioCaptor.getValue().isAtivo()).isFalse();
    }

    @Test
    void deveRetornarFuncionarioInativoAposExclusaoLogica() {
        Funcionario funcionario = criarFuncionarioAtivo();
        Funcionario funcionarioInativado = criarFuncionarioInativo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionario));
        when(repository.salvar(any())).thenReturn(funcionarioInativado);

        Funcionario resultado = service.excluirFuncionario(ID_EXISTENTE);

        assertThat(resultado.isAtivo()).isFalse();
    }

    @Test
    void deveLancarFuncionarioNotFoundAoExcluirQuandoIdNaoExistir() {
        when(repository.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluirFuncionario(ID_INEXISTENTE))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveLancarFuncionarioInativoExceptionAoExcluirQuandoFuncionarioJaEstiverInativo() {
        Funcionario funcionarioInativo = criarFuncionarioInativo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionarioInativo));

        assertThatThrownBy(() -> service.excluirFuncionario(ID_EXISTENTE))
                .isInstanceOf(FuncionarioInativoException.class)
                .hasMessage("O Funcionário com ID " + ID_EXISTENTE + " encontra-se inativo");

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveReativarFuncionarioInativoComSucesso() {
        Funcionario funcionarioInativo = criarFuncionarioInativo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionarioInativo));
        when(repository.salvar(any())).thenReturn(funcionarioInativo);

        service.reativarFuncionario(ID_EXISTENTE);

        verify(repository).salvar(funcionarioCaptor.capture());
        assertThat(funcionarioCaptor.getValue().isAtivo()).isTrue();
    }

    @Test
    void deveRetornarFuncionarioAtivoAposReativacao() {
        Funcionario funcionarioInativo = criarFuncionarioInativo();
        Funcionario funcionarioReativado = criarFuncionarioAtivo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionarioInativo));
        when(repository.salvar(any())).thenReturn(funcionarioReativado);

        Funcionario resultado = service.reativarFuncionario(ID_EXISTENTE);

        assertThat(resultado.isAtivo()).isTrue();
    }

    @Test
    void deveLancarFuncionarioNotFoundAoReativarQuandoIdNaoExistir() {
        when(repository.buscarPorId(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reativarFuncionario(ID_INEXISTENTE))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveLancarFuncionarioJaAtivoExceptionAoReativarQuandoFuncionarioJaEstiverAtivo() {
        Funcionario funcionarioAtivo = criarFuncionarioAtivo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionarioAtivo));

        assertThatThrownBy(() -> service.reativarFuncionario(ID_EXISTENTE))
                .isInstanceOf(FuncionarioJaAtivoException.class)
                .hasMessage("O Funcionário com id " + ID_EXISTENTE + " já se encontra ativo.");

        verify(repository, never()).salvar(any());
    }

    @Test
    void devePersistirFuncionarioComAtivoTrueAoReativar() {
        Funcionario funcionarioInativo = criarFuncionarioInativo();
        when(repository.buscarPorId(ID_EXISTENTE)).thenReturn(Optional.of(funcionarioInativo));
        when(repository.salvar(any())).thenReturn(funcionarioInativo);

        service.reativarFuncionario(ID_EXISTENTE);

        verify(repository).salvar(funcionarioCaptor.capture());
        Funcionario salvo = funcionarioCaptor.getValue();
        assertThat(salvo.isAtivo()).isTrue();
        assertThat(salvo.getId()).isEqualTo(ID_EXISTENTE);
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

    private AtualizarFuncionarioCommand criarDtoCompleto() {
        return new AtualizarFuncionarioCommand(
                EMAIL_NOVO,
                SENHA_NOVA,
                NOME_NOVO,
                FUNCAO_NOVA);
    }
}
