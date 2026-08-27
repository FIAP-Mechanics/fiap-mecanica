package com.fiap.mecanica.atendimento.application.usecase;

import com.fiap.mecanica.atendimento.application.command.AtualizarTemplateCommand;
import com.fiap.mecanica.atendimento.application.port.out.TemplateGateway;
import com.fiap.mecanica.atendimento.domain.CodigoTemplate;
import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;
import com.fiap.mecanica.atendimento.exception.TemplateNotFound;
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
    private TemplateGateway templateGateway;

    @InjectMocks
    private TemplateInteractor templateInteractor;

    // ===================== buscarTodos =====================

    @Test
    void deveRetornarListaDeTemplatesComSucesso() {
        List<TemplateNotificacao> templates = List.of(criarTemplate());
        when(templateGateway.buscarTodos()).thenReturn(templates);

        List<TemplateNotificacao> resultado = templateInteractor.buscarTodos();

        assertThat(resultado).hasSize(1);
        verify(templateGateway).buscarTodos();
    }

    // ===================== buscarPorCodigo =====================

    @Test
    void deveRetornarTemplateQuandoCodigoExistir() {
        TemplateNotificacao template = criarTemplate();
        when(templateGateway.buscarPorCodigo(CODIGO.name())).thenReturn(Optional.of(template));

        TemplateNotificacao resultado = templateInteractor.buscarPorCodigo(CODIGO);

        assertThat(resultado.getCodigo()).isEqualTo(CODIGO.name());
        verify(templateGateway).buscarPorCodigo(CODIGO.name());
    }

    @Test
    void deveLancarExcecaoQuandoCodigoNaoExistir() {
        when(templateGateway.buscarPorCodigo(CODIGO.name())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateInteractor.buscarPorCodigo(CODIGO))
                .isInstanceOf(TemplateNotFound.class);
    }

    // ===================== cadastrar =====================

    @Test
    void deveCadastrarTemplateComSucesso() {
        TemplateNotificacao template = criarTemplate();
        when(templateGateway.salvar(any(TemplateNotificacao.class))).thenReturn(template);

        TemplateNotificacao resultado = templateInteractor.cadastrar(template);

        assertThat(resultado).isNotNull();
        verify(templateGateway).salvar(template);
    }

    // ===================== atualizar =====================

    @Test
    void deveAtualizarTemplateComSucesso() {
        TemplateNotificacao templateExistente = criarTemplate();
        AtualizarTemplateCommand command = new AtualizarTemplateCommand("Novo conteudo");

        when(templateGateway.buscarPorCodigo(CODIGO.name())).thenReturn(Optional.of(templateExistente));
        when(templateGateway.salvar(any(TemplateNotificacao.class))).thenAnswer(i -> i.getArguments()[0]);

        TemplateNotificacao resultado = templateInteractor.atualizar(CODIGO, command);

        assertThat(resultado.getConteudo()).isEqualTo("Novo conteudo");
        verify(templateGateway).salvar(templateExistente);
    }

    // ===================== deletar =====================

    @Test
    void deveDeletarTemplateComSucesso() {
        TemplateNotificacao template = criarTemplate();
        when(templateGateway.buscarPorCodigo(CODIGO.name())).thenReturn(Optional.of(template));

        templateInteractor.deletar(CODIGO);

        verify(templateGateway).deletar(template);
    }

    private TemplateNotificacao criarTemplate() {
        return TemplateNotificacao.builder()
                .id(1L)
                .codigo(CODIGO.name())
                .conteudo(CONTEUDO)
                .build();
    }
}
