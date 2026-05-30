package com.fiap.mecanica.controller;

import com.fiap.mecanica.controller.request.AtualizarFuncionarioRequest;
import com.fiap.mecanica.controller.request.CadastrarFuncionarioRequest;
import com.fiap.mecanica.domain.Funcao;
import com.fiap.mecanica.domain.Funcionario;
import com.fiap.mecanica.dto.FuncionarioDto;
import com.fiap.mecanica.exception.FuncionarioInativoException;
import com.fiap.mecanica.exception.FuncionarioJaAtivoException;
import com.fiap.mecanica.exception.FuncionarioNotFound;
import com.fiap.mecanica.service.FuncionarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private FuncionarioService service;

    @InjectMocks
    private FuncionarioController controller;

    @Captor
    private ArgumentCaptor<Funcionario> funcionarioCaptor;

    @Test
    void deveRetornarFuncionarioDtoQuandoIdExistir() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(service.buscarFuncionarioPorId(ID_EXISTENTE)).thenReturn(funcionario);

        FuncionarioDto resultado = controller.get(ID_EXISTENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.email()).isEqualTo(EMAIL);
        assertThat(resultado.nome()).isEqualTo(NOME);
        assertThat(resultado.funcao()).isEqualTo(FUNCAO);
        verify(service).buscarFuncionarioPorId(ID_EXISTENTE);
    }

    @Test
    void deveOmitirSenhaNoRetornoDoGet() {
        Funcionario funcionario = criarFuncionarioAtivo();
        when(service.buscarFuncionarioPorId(ID_EXISTENTE)).thenReturn(funcionario);

        FuncionarioDto resultado = controller.get(ID_EXISTENTE);

        assertThat(resultado.senha()).isNull();
    }

    @Test
    void deveLancarFuncionarioNotFoundNoGetQuandoIdNaoExistir() {
        when(service.buscarFuncionarioPorId(ID_INEXISTENTE))
                .thenThrow(new FuncionarioNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.get(ID_INEXISTENTE))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(service).buscarFuncionarioPorId(ID_INEXISTENTE);
    }

    @Test
    void deveLancarFuncionarioInativoExceptionNoGetQuandoFuncionarioEstiverInativo() {
        when(service.buscarFuncionarioPorId(ID_EXISTENTE))
                .thenThrow(new FuncionarioInativoException(ID_EXISTENTE));

        assertThatThrownBy(() -> controller.get(ID_EXISTENTE))
                .isInstanceOf(FuncionarioInativoException.class)
                .hasMessage("O Funcionário com ID " + ID_EXISTENTE + " encontra-se inativo");

        verify(service).buscarFuncionarioPorId(ID_EXISTENTE);
    }

    @Test
    void deveCadastrarFuncionarioERetornarDtoComSucesso() {
        CadastrarFuncionarioRequest request = criarCadastrarRequest();
        Funcionario funcionarioSalvo = criarFuncionarioAtivo();
        when(service.cadastrarFuncionario(any(Funcionario.class))).thenReturn(funcionarioSalvo);

        FuncionarioDto resultado = controller.create(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.email()).isEqualTo(EMAIL);
        assertThat(resultado.nome()).isEqualTo(NOME);
        assertThat(resultado.funcao()).isEqualTo(FUNCAO);
        verify(service).cadastrarFuncionario(any(Funcionario.class));
    }

    @Test
    void deveConverterRequestParaEntidadeCorretamenteAoCadastrar() {
        CadastrarFuncionarioRequest request = criarCadastrarRequest();
        Funcionario funcionarioSalvo = criarFuncionarioAtivo();
        when(service.cadastrarFuncionario(funcionarioCaptor.capture())).thenReturn(funcionarioSalvo);

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
        when(service.cadastrarFuncionario(any(Funcionario.class))).thenReturn(criarFuncionarioAtivo());

        FuncionarioDto resultado = controller.create(request);

        assertThat(resultado.senha()).isNull();
    }

    @Test
    void deveAtualizarFuncionarioERetornarDtoComSucesso() {
        AtualizarFuncionarioRequest request = criarAtualizarRequestCompleto();
        Funcionario funcionarioAtualizado = criarFuncionarioAtualizado();
        when(service.atualizarFuncionario(eq(ID_EXISTENTE), any(FuncionarioDto.class)))
                .thenReturn(funcionarioAtualizado);

        FuncionarioDto resultado = controller.update(ID_EXISTENTE, request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.email()).isEqualTo(EMAIL_NOVO);
        assertThat(resultado.nome()).isEqualTo(NOME_NOVO);
        assertThat(resultado.funcao()).isEqualTo(FUNCAO_NOVA);
        verify(service).atualizarFuncionario(eq(ID_EXISTENTE), any(FuncionarioDto.class));
    }

    @Test
    void deveConverterRequestParaDtoCorretamenteAoAtualizar() {
        AtualizarFuncionarioRequest request = criarAtualizarRequestCompleto();
        ArgumentCaptor<FuncionarioDto> dtoCaptor = ArgumentCaptor.forClass(FuncionarioDto.class);
        when(service.atualizarFuncionario(eq(ID_EXISTENTE), dtoCaptor.capture()))
                .thenReturn(criarFuncionarioAtualizado());

        controller.update(ID_EXISTENTE, request);

        FuncionarioDto dto = dtoCaptor.getValue();
        assertThat(dto.email()).isEqualTo(EMAIL_NOVO);
        assertThat(dto.nome()).isEqualTo(NOME_NOVO);
        assertThat(dto.senha()).isEqualTo(SENHA_NOVA);
        assertThat(dto.funcao()).isEqualTo(FUNCAO_NOVA);
    }

    @Test
    void deveLancarFuncionarioNotFoundNoUpdateQuandoIdNaoExistir() {
        AtualizarFuncionarioRequest request = criarAtualizarRequestCompleto();
        when(service.atualizarFuncionario(eq(ID_INEXISTENTE), any(FuncionarioDto.class)))
                .thenThrow(new FuncionarioNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.update(ID_INEXISTENTE, request))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);
    }

    @Test
    void deveLancarFuncionarioInativoExceptionNoUpdateQuandoFuncionarioEstiverInativo() {
        AtualizarFuncionarioRequest request = criarAtualizarRequestCompleto();
        when(service.atualizarFuncionario(eq(ID_EXISTENTE), any(FuncionarioDto.class)))
                .thenThrow(new FuncionarioInativoException(ID_EXISTENTE));

        assertThatThrownBy(() -> controller.update(ID_EXISTENTE, request))
                .isInstanceOf(FuncionarioInativoException.class)
                .hasMessage("O Funcionário com ID " + ID_EXISTENTE + " encontra-se inativo");
    }

    @Test
    void deveOmitirSenhaNoRetornoDoUpdate() {
        AtualizarFuncionarioRequest request = criarAtualizarRequestCompleto();
        when(service.atualizarFuncionario(eq(ID_EXISTENTE), any(FuncionarioDto.class)))
                .thenReturn(criarFuncionarioAtualizado());

        FuncionarioDto resultado = controller.update(ID_EXISTENTE, request);

        assertThat(resultado.senha()).isNull();
    }

    @Test
    void deveExcluirFuncionarioLogicamenteERetornarDtoComSucesso() {
        Funcionario funcionarioInativado = criarFuncionarioInativo();
        when(service.excluirFuncionario(ID_EXISTENTE)).thenReturn(funcionarioInativado);

        FuncionarioDto resultado = controller.delete(ID_EXISTENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        verify(service).excluirFuncionario(ID_EXISTENTE);
    }

    @Test
    void deveLancarFuncionarioNotFoundNoDeleteQuandoIdNaoExistir() {
        when(service.excluirFuncionario(ID_INEXISTENTE))
                .thenThrow(new FuncionarioNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.delete(ID_INEXISTENTE))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(service).excluirFuncionario(ID_INEXISTENTE);
    }

    @Test
    void deveLancarFuncionarioInativoExceptionNoDeleteQuandoFuncionarioJaEstiverInativo() {
        when(service.excluirFuncionario(ID_EXISTENTE))
                .thenThrow(new FuncionarioInativoException(ID_EXISTENTE));

        assertThatThrownBy(() -> controller.delete(ID_EXISTENTE))
                .isInstanceOf(FuncionarioInativoException.class)
                .hasMessage("O Funcionário com ID " + ID_EXISTENTE + " encontra-se inativo");
    }

    @Test
    void deveOmitirSenhaNoRetornoDoDelete() {
        when(service.excluirFuncionario(ID_EXISTENTE)).thenReturn(criarFuncionarioInativo());

        FuncionarioDto resultado = controller.delete(ID_EXISTENTE);

        assertThat(resultado.senha()).isNull();
    }

    @Test
    void deveReativarFuncionarioERetornarDtoComSucesso() {
        Funcionario funcionarioReativado = criarFuncionarioAtivo();
        when(service.reativarFuncionario(ID_EXISTENTE)).thenReturn(funcionarioReativado);

        FuncionarioDto resultado = controller.reativar(ID_EXISTENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.email()).isEqualTo(EMAIL);
        assertThat(resultado.nome()).isEqualTo(NOME);
        verify(service).reativarFuncionario(ID_EXISTENTE);
    }

    @Test
    void deveLancarFuncionarioNotFoundNoReativarQuandoIdNaoExistir() {
        when(service.reativarFuncionario(ID_INEXISTENTE))
                .thenThrow(new FuncionarioNotFound(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.reativar(ID_INEXISTENTE))
                .isInstanceOf(FuncionarioNotFound.class)
                .hasMessage("Funcionário não encontrado com ID: " + ID_INEXISTENTE);

        verify(service).reativarFuncionario(ID_INEXISTENTE);
    }

    @Test
    void deveLancarFuncionarioJaAtivoExceptionNoReativarQuandoFuncionarioJaEstiverAtivo() {
        when(service.reativarFuncionario(ID_EXISTENTE))
                .thenThrow(new FuncionarioJaAtivoException(ID_EXISTENTE));

        assertThatThrownBy(() -> controller.reativar(ID_EXISTENTE))
                .isInstanceOf(FuncionarioJaAtivoException.class)
                .hasMessage("O Funcionário com id " + ID_EXISTENTE + " já se encontra ativo.");

        verify(service).reativarFuncionario(ID_EXISTENTE);
    }

    @Test
    void deveOmitirSenhaNoRetornoDoReativar() {
        when(service.reativarFuncionario(ID_EXISTENTE)).thenReturn(criarFuncionarioAtivo());

        FuncionarioDto resultado = controller.reativar(ID_EXISTENTE);

        assertThat(resultado.senha()).isNull();
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
