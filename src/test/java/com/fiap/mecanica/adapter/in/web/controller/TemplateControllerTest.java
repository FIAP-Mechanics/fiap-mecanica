package com.fiap.mecanica.adapter.in.web.controller;

import com.fiap.mecanica.adapter.in.web.request.AtualizarTemplateRequest;
import com.fiap.mecanica.adapter.in.web.request.CriarTemplateRequest;
import com.fiap.mecanica.adapter.in.web.response.TemplateDto;
import com.fiap.mecanica.application.command.AtualizarTemplateCommand;
import com.fiap.mecanica.application.exception.TemplateNotFound;
import com.fiap.mecanica.application.port.in.TemplateUseCase;
import com.fiap.mecanica.domain.CodigoTemplate;
import com.fiap.mecanica.domain.TemplateNotificacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateControllerTest {

    private static final CodigoTemplate CODIGO = CodigoTemplate.AUTORIZAR_ORCAMENTO;
    private static final String CONTEUDO = "Conteudo teste";

    @Mock
    private TemplateUseCase service;

    @InjectMocks
    private TemplateController controller;

    @Test
    void deveRetornarListaDeTemplatesComSucesso() {
        // Arrange
        List<TemplateNotificacao> templates = List.of(criarTemplate());
        when(service.buscarTodos()).thenReturn(templates);

        // Act
        List<TemplateDto> resultado = controller.getList();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().codigo()).isEqualTo(CODIGO);
        verify(service).buscarTodos();
    }

    @Test
    void deveRetornarTemplatePorCodigoComSucesso() {
        // Arrange
        TemplateNotificacao template = criarTemplate();
        when(service.buscarPorCodigo(CODIGO)).thenReturn(template);

        // Act
        TemplateDto resultado = controller.get(CODIGO);

        // Assert
        assertThat(resultado.codigo()).isEqualTo(CODIGO);
        assertThat(resultado.conteudo()).isEqualTo(CONTEUDO);
        verify(service).buscarPorCodigo(CODIGO);
    }

    @Test
    void deveLancarExcecaoQuandoTemplateNaoEncontradoNoGet() {
        // Arrange
        when(service.buscarPorCodigo(CODIGO)).thenThrow(new TemplateNotFound(CODIGO));

        // Act & Assert
        assertThatThrownBy(() -> controller.get(CODIGO))
                .isInstanceOf(TemplateNotFound.class);
    }

    @Test
    void deveCadastrarTemplateComSucesso() {
        // Arrange
        CriarTemplateRequest request = CriarTemplateRequest.builder()
                .codigo(CODIGO)
                .conteudo(CONTEUDO)
                .build();
        TemplateNotificacao templateSalvo = criarTemplate();
        when(service.cadastrar(any(TemplateNotificacao.class))).thenReturn(templateSalvo);

        // Act
        TemplateDto resultado = controller.create(request);

        // Assert
        assertThat(resultado.codigo()).isEqualTo(CODIGO);
        verify(service).cadastrar(any(TemplateNotificacao.class));
    }

    @Test
    void deveAtualizarTemplateComSucesso() {
        // Arrange
        AtualizarTemplateRequest request = AtualizarTemplateRequest.builder()
                .conteudo("Novo conteudo")
                .build();
        TemplateNotificacao templateAtualizado = criarTemplate();
        templateAtualizado.setConteudo("Novo conteudo");

        when(service.atualizar(eq(CODIGO), any(AtualizarTemplateCommand.class))).thenReturn(templateAtualizado);

        // Act
        TemplateDto resultado = controller.update(CODIGO, request);

        // Assert
        assertThat(resultado.conteudo()).isEqualTo("Novo conteudo");
        verify(service).atualizar(eq(CODIGO), any(AtualizarTemplateCommand.class));
    }

    @Test
    void deveDeletarTemplateComSucesso() {
        // Act
        controller.delete(CODIGO);

        // Assert
        verify(service).deletar(CODIGO);
    }

    private TemplateNotificacao criarTemplate() {
        return TemplateNotificacao.builder()
                .id(1L)
                .codigo(CODIGO.name())
                .conteudo(CONTEUDO)
                .build();
    }
}
