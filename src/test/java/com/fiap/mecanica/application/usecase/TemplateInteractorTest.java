package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AtualizarTemplateCommand;
import com.fiap.mecanica.application.port.out.TemplateGateway;
import com.fiap.mecanica.domain.CodigoTemplate;
import com.fiap.mecanica.domain.TemplateNotificacao;
import com.fiap.mecanica.application.exception.TemplateNotFound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateInteractorTest {

    private static final CodigoTemplate CODIGO = CodigoTemplate.AUTORIZAR_ORCAMENTO;
    private static final String CONTEUDO = "Conteudo do template";

    @Mock
    private TemplateGateway gateway;

    @InjectMocks
    private TemplateInteractor interactor;

    // ===================== buscarTodos =====================

    @Test
    void deveRetornarListaDeTemplatesComSucesso() {
        // Arrange
        List<TemplateNotificacao> templates = List.of(criarTemplate());
        when(gateway.buscarTodos()).thenReturn(templates);

        // Act
        List<TemplateNotificacao> resultado = interactor.buscarTodos();

        // Assert
        assertThat(resultado).hasSize(1);
        verify(gateway).buscarTodos();
    }

    // ===================== buscarPorCodigo =====================

    @Test
    void deveRetornarTemplateQuandoCodigoExistir() {
        // Arrange
        TemplateNotificacao template = criarTemplate();
        when(gateway.buscarPorCodigo(CODIGO)).thenReturn(Optional.of(template));

        // Act
        TemplateNotificacao resultado = interactor.buscarPorCodigo(CODIGO);

        // Assert
        assertThat(resultado.getCodigo()).isEqualTo(CODIGO.name());
        verify(gateway).buscarPorCodigo(CODIGO);
    }

    @Test
    void deveLancarExcecaoQuandoCodigoNaoExistir() {
        // Arrange
        when(gateway.buscarPorCodigo(CODIGO)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> interactor.buscarPorCodigo(CODIGO))
                .isInstanceOf(TemplateNotFound.class);
    }

    // ===================== cadastrar =====================

    @Test
    void deveCadastrarTemplateComSucesso() {
        // Arrange
        TemplateNotificacao template = criarTemplate();
        when(gateway.salvar(any(TemplateNotificacao.class))).thenReturn(template);

        // Act
        TemplateNotificacao resultado = interactor.cadastrar(template);

        // Assert
        assertThat(resultado).isNotNull();
        verify(gateway).salvar(template);
    }

    // ===================== atualizar =====================

    @Test
    void deveAtualizarTemplateComSucesso() {
        // Arrange
        TemplateNotificacao templateExistente = criarTemplate();
        AtualizarTemplateCommand command = new AtualizarTemplateCommand("Novo conteudo");
        
        when(gateway.buscarPorCodigo(CODIGO)).thenReturn(Optional.of(templateExistente));
        when(gateway.salvar(any(TemplateNotificacao.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        TemplateNotificacao resultado = interactor.atualizar(CODIGO, command);

        // Assert
        assertThat(resultado.getConteudo()).isEqualTo("Novo conteudo");
        verify(gateway).salvar(templateExistente);
    }

    // ===================== deletar =====================

    @Test
    void deveDeletarTemplateComSucesso() {
        // Arrange
        TemplateNotificacao template = criarTemplate();
        when(gateway.buscarPorCodigo(CODIGO)).thenReturn(Optional.of(template));

        // Act
        interactor.deletar(CODIGO);

        // Assert
        verify(gateway).excluir(template);
    }

    // Métodos auxiliares privados

    private TemplateNotificacao criarTemplate() {
        return TemplateNotificacao.builder()
                .id(1L)
                .codigo(CODIGO.name())
                .conteudo(CONTEUDO)
                .build();
    }
}
