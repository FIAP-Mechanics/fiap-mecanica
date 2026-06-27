package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.TemplateNotificacao;
import com.fiap.mecanica.dto.TemplateDto;
import com.fiap.mecanica.exception.TemplateNotFound;
import com.fiap.mecanica.infra.configs.enums.CodigoTemplate;
import com.fiap.mecanica.repository.TemplateRepository;
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
        // Arrange
        List<TemplateNotificacao> templates = List.of(criarTemplate());
        when(repository.findAll()).thenReturn(templates);

        // Act
        List<TemplateNotificacao> resultado = templateService.buscarTodos();

        // Assert
        assertThat(resultado).hasSize(1);
        verify(repository).findAll();
    }

    // ===================== buscarPorCodigo =====================

    @Test
    void deveRetornarTemplateQuandoCodigoExistir() {
        // Arrange
        TemplateNotificacao template = criarTemplate();
        when(repository.findByCodigo(CODIGO.name())).thenReturn(Optional.of(template));

        // Act
        TemplateNotificacao resultado = templateService.buscarPorCodigo(CODIGO);

        // Assert
        assertThat(resultado.getCodigo()).isEqualTo(CODIGO.name());
        verify(repository).findByCodigo(CODIGO.name());
    }

    @Test
    void deveLancarExcecaoQuandoCodigoNaoExistir() {
        // Arrange
        when(repository.findByCodigo(CODIGO.name())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> templateService.buscarPorCodigo(CODIGO))
                .isInstanceOf(TemplateNotFound.class);
    }

    // ===================== cadastrar =====================

    @Test
    void deveCadastrarTemplateComSucesso() {
        // Arrange
        TemplateNotificacao template = criarTemplate();
        when(repository.save(any(TemplateNotificacao.class))).thenReturn(template);

        // Act
        TemplateNotificacao resultado = templateService.cadastrar(template);

        // Assert
        assertThat(resultado).isNotNull();
        verify(repository).save(template);
    }

    // ===================== atualizar =====================

    @Test
    void deveAtualizarTemplateComSucesso() {
        // Arrange
        TemplateNotificacao templateExistente = criarTemplate();
        TemplateDto dto = TemplateDto.builder().conteudo("Novo conteudo").build();
        
        when(repository.findByCodigo(CODIGO.name())).thenReturn(Optional.of(templateExistente));
        when(repository.save(any(TemplateNotificacao.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        TemplateNotificacao resultado = templateService.atualizar(CODIGO, dto);

        // Assert
        assertThat(resultado.getConteudo()).isEqualTo("Novo conteudo");
        verify(repository).save(templateExistente);
    }

    // ===================== deletar =====================

    @Test
    void deveDeletarTemplateComSucesso() {
        // Arrange
        TemplateNotificacao template = criarTemplate();
        when(repository.findByCodigo(CODIGO.name())).thenReturn(Optional.of(template));

        // Act
        templateService.deletar(CODIGO);

        // Assert
        verify(repository).delete(template);
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
