package com.fiap.mecanica.atendimento.service;

import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;
import com.fiap.mecanica.atendimento.dto.TemplateDto;
import com.fiap.mecanica.atendimento.exception.TemplateNotFound;
import com.fiap.mecanica.atendimento.infra.enums.CodigoTemplate;
import com.fiap.mecanica.atendimento.repository.TemplateRepository;
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
class TemplateServiceTest {

    private static final CodigoTemplate CODIGO = CodigoTemplate.AUTORIZAR_ORCAMENTO;
    private static final String CONTEUDO = "Conteudo do template";

    @Mock
    private TemplateRepository repository;

    @InjectMocks
    private TemplateService templateService;

    // ===================== buscarTodos =====================

    @Test
    void deveRetornarListaDeTemplatesComSucesso() {
        List<TemplateNotificacao> templates = List.of(criarTemplate());
        when(repository.findAll()).thenReturn(templates);

        List<TemplateNotificacao> resultado = templateService.buscarTodos();

        assertThat(resultado).hasSize(1);
        verify(repository).findAll();
    }

    // ===================== buscarPorCodigo =====================

    @Test
    void deveRetornarTemplateQuandoCodigoExistir() {
        TemplateNotificacao template = criarTemplate();
        when(repository.findByCodigo(CODIGO.name())).thenReturn(Optional.of(template));

        TemplateNotificacao resultado = templateService.buscarPorCodigo(CODIGO);

        assertThat(resultado.getCodigo()).isEqualTo(CODIGO.name());
        verify(repository).findByCodigo(CODIGO.name());
    }

    @Test
    void deveLancarExcecaoQuandoCodigoNaoExistir() {
        when(repository.findByCodigo(CODIGO.name())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.buscarPorCodigo(CODIGO))
                .isInstanceOf(TemplateNotFound.class);
    }

    // ===================== cadastrar =====================

    @Test
    void deveCadastrarTemplateComSucesso() {
        TemplateNotificacao template = criarTemplate();
        when(repository.save(any(TemplateNotificacao.class))).thenReturn(template);

        TemplateNotificacao resultado = templateService.cadastrar(template);

        assertThat(resultado).isNotNull();
        verify(repository).save(template);
    }

    // ===================== atualizar =====================

    @Test
    void deveAtualizarTemplateComSucesso() {
        TemplateNotificacao templateExistente = criarTemplate();
        TemplateDto dto = TemplateDto.builder().conteudo("Novo conteudo").build();

        when(repository.findByCodigo(CODIGO.name())).thenReturn(Optional.of(templateExistente));
        when(repository.save(any(TemplateNotificacao.class))).thenAnswer(i -> i.getArguments()[0]);

        TemplateNotificacao resultado = templateService.atualizar(CODIGO, dto);

        assertThat(resultado.getConteudo()).isEqualTo("Novo conteudo");
        verify(repository).save(templateExistente);
    }

    // ===================== deletar =====================

    @Test
    void deveDeletarTemplateComSucesso() {
        TemplateNotificacao template = criarTemplate();
        when(repository.findByCodigo(CODIGO.name())).thenReturn(Optional.of(template));

        templateService.deletar(CODIGO);

        verify(repository).delete(template);
    }

    private TemplateNotificacao criarTemplate() {
        return TemplateNotificacao.builder()
                .id(1L)
                .codigo(CODIGO.name())
                .conteudo(CONTEUDO)
                .build();
    }
}
