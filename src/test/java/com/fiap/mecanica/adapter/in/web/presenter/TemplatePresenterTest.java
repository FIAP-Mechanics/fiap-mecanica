package com.fiap.mecanica.adapter.in.web.presenter;

import com.fiap.mecanica.adapter.in.web.request.CriarTemplateRequest;
import com.fiap.mecanica.adapter.in.web.response.TemplateDto;
import com.fiap.mecanica.domain.CodigoTemplate;
import com.fiap.mecanica.domain.TemplateNotificacao;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplatePresenterTest {

    @Test
    void deveConverterRequestParaEntidade() {
        CriarTemplateRequest request = CriarTemplateRequest.builder()
                .codigo(CodigoTemplate.RETIRAR_VEICULO)
                .conteudo("Conteudo teste")
                .build();

        TemplateNotificacao entidade = TemplatePresenter.toEntity(request);

        assertThat(entidade.getCodigo()).isEqualTo(CodigoTemplate.RETIRAR_VEICULO.name());
        assertThat(entidade.getConteudo()).isEqualTo("Conteudo teste");
    }

    @Test
    void deveConverterEntidadeParaDto() {
        TemplateNotificacao entidade = TemplateNotificacao.builder()
                .codigo(CodigoTemplate.VEICULO_RETIRADO.name())
                .conteudo("Conteudo DTO")
                .build();

        TemplateDto dto = TemplatePresenter.toDto(entidade);

        assertThat(dto.codigo()).isEqualTo(CodigoTemplate.VEICULO_RETIRADO);
        assertThat(dto.conteudo()).isEqualTo("Conteudo DTO");
    }
}
