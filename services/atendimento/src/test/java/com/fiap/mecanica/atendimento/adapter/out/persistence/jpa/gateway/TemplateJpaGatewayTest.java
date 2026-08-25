package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity.TemplateNotificacaoJpaEntity;
import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.repository.TemplateSpringDataRepository;
import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateJpaGatewayTest {

    private static final Long ID = 1L;
    private static final String CODIGO = "AUTORIZAR_ORCAMENTO";
    private static final String CONTEUDO = "Autorize o orcamento pelo link";

    @Mock
    private TemplateSpringDataRepository repository;

    @InjectMocks
    private TemplateJpaGateway gateway;

    @Captor
    private ArgumentCaptor<TemplateNotificacaoJpaEntity> entityCaptor;

    // ===================== buscarTodos =====================

    @Test
    void deveRetornarTodosOsTemplatesConvertidosParaDomain() {
        when(repository.findAll()).thenReturn(List.of(criarEntity()));

        List<TemplateNotificacao> resultado = gateway.buscarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getCodigo()).isEqualTo(CODIGO);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverTemplates() {
        when(repository.findAll()).thenReturn(List.of());

        List<TemplateNotificacao> resultado = gateway.buscarTodos();

        assertThat(resultado).isEmpty();
    }

    // ===================== buscarPorCodigo =====================

    @Test
    void deveRetornarTemplateQuandoCodigoExistir() {
        when(repository.findByCodigo(CODIGO)).thenReturn(Optional.of(criarEntity()));

        Optional<TemplateNotificacao> resultado = gateway.buscarPorCodigo(CODIGO);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCodigo()).isEqualTo(CODIGO);
    }

    @Test
    void deveRetornarOptionalVazioQuandoCodigoNaoExistir() {
        when(repository.findByCodigo(CODIGO)).thenReturn(Optional.empty());

        Optional<TemplateNotificacao> resultado = gateway.buscarPorCodigo(CODIGO);

        assertThat(resultado).isEmpty();
    }

    // ===================== salvar =====================

    @Test
    void deveSalvarTemplateERetornarDomainConvertido() {
        TemplateNotificacao template = criarTemplate();
        when(repository.save(any())).thenReturn(criarEntity());

        TemplateNotificacao resultado = gateway.salvar(template);

        assertThat(resultado.getId()).isEqualTo(ID);
        assertThat(resultado.getCodigo()).isEqualTo(CODIGO);
        assertThat(resultado.getConteudo()).isEqualTo(CONTEUDO);
    }

    // ===================== deletar =====================

    @Test
    void deveDeletarTemplateConvertidoParaEntity() {
        TemplateNotificacao template = criarTemplate();

        gateway.deletar(template);

        verify(repository).delete(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getId()).isEqualTo(ID);
        assertThat(entityCaptor.getValue().getCodigo()).isEqualTo(CODIGO);
    }

    private TemplateNotificacaoJpaEntity criarEntity() {
        return TemplateNotificacaoJpaEntity.builder()
                .id(ID)
                .codigo(CODIGO)
                .conteudo(CONTEUDO)
                .build();
    }

    private TemplateNotificacao criarTemplate() {
        return TemplateNotificacao.builder()
                .id(ID)
                .codigo(CODIGO)
                .conteudo(CONTEUDO)
                .build();
    }
}
