package com.fiap.mecanica.atendimento.controller;

import com.fiap.mecanica.atendimento.controller.request.AtualizarTemplateRequest;
import com.fiap.mecanica.atendimento.controller.request.CriarTemplateRequest;
import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;
import com.fiap.mecanica.atendimento.dto.TemplateDto;
import com.fiap.mecanica.atendimento.exception.TemplateNotFound;
import com.fiap.mecanica.atendimento.infra.enums.CodigoTemplate;
import com.fiap.mecanica.atendimento.service.TemplateService;
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
    private TemplateService service;

    @InjectMocks
    private TemplateController controller;

    @Test
    void deveRetornarListaDeTemplatesComSucesso() {
        List<TemplateNotificacao> templates = List.of(criarTemplate());
        when(service.buscarTodos()).thenReturn(templates);

        List<TemplateDto> resultado = controller.getList();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().codigo()).isEqualTo(CODIGO);
        verify(service).buscarTodos();
    }

    @Test
    void deveRetornarTemplatePorCodigoComSucesso() {
        TemplateNotificacao template = criarTemplate();
        when(service.buscarPorCodigo(CODIGO)).thenReturn(template);

        TemplateDto resultado = controller.get(CODIGO);

        assertThat(resultado.codigo()).isEqualTo(CODIGO);
        assertThat(resultado.conteudo()).isEqualTo(CONTEUDO);
        verify(service).buscarPorCodigo(CODIGO);
    }

    @Test
    void deveLancarExcecaoQuandoTemplateNaoEncontradoNoGet() {
        when(service.buscarPorCodigo(CODIGO)).thenThrow(new TemplateNotFound(CODIGO));

        assertThatThrownBy(() -> controller.get(CODIGO))
                .isInstanceOf(TemplateNotFound.class);
    }

    @Test
    void deveCadastrarTemplateComSucesso() {
        CriarTemplateRequest request = CriarTemplateRequest.builder()
                .codigo(CODIGO)
                .conteudo(CONTEUDO)
                .build();
        TemplateNotificacao templateSalvo = criarTemplate();
        when(service.cadastrar(any(TemplateNotificacao.class))).thenReturn(templateSalvo);

        TemplateDto resultado = controller.create(request);

        assertThat(resultado.codigo()).isEqualTo(CODIGO);
        verify(service).cadastrar(any(TemplateNotificacao.class));
    }

    @Test
    void deveAtualizarTemplateComSucesso() {
        AtualizarTemplateRequest request = AtualizarTemplateRequest.builder()
                .conteudo("Novo conteudo")
                .build();
        TemplateNotificacao templateAtualizado = criarTemplate();
        templateAtualizado.setConteudo("Novo conteudo");

        when(service.atualizar(eq(CODIGO), any(TemplateDto.class))).thenReturn(templateAtualizado);

        TemplateDto resultado = controller.update(CODIGO, request);

        assertThat(resultado.conteudo()).isEqualTo("Novo conteudo");
        verify(service).atualizar(eq(CODIGO), any(TemplateDto.class));
    }

    @Test
    void deveDeletarTemplateComSucesso() {
        controller.delete(CODIGO);

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
