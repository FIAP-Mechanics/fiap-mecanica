package com.fiap.mecanica.atendimento.controller.mapper;

import com.fiap.mecanica.atendimento.controller.request.CriarTemplateRequest;
import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;
import com.fiap.mecanica.atendimento.dto.TemplateDto;
import com.fiap.mecanica.atendimento.infra.enums.CodigoTemplate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateMapperTest {

    @Test
    void deveConverterRequestParaEntidade() {
        CriarTemplateRequest request = CriarTemplateRequest.builder()
                .codigo(CodigoTemplate.RETIRAR_VEICULO)
                .conteudo("Conteudo teste")
                .build();

        TemplateNotificacao entidade = TemplateMapper.toEntity(request);

        assertThat(entidade.getCodigo()).isEqualTo(CodigoTemplate.RETIRAR_VEICULO.name());
        assertThat(entidade.getConteudo()).isEqualTo("Conteudo teste");
    }

    @Test
    void deveConverterEntidadeParaDto() {
        TemplateNotificacao entidade = TemplateNotificacao.builder()
                .codigo(CodigoTemplate.VEICULO_RETIRADO.name())
                .conteudo("Conteudo DTO")
                .build();

        TemplateDto dto = TemplateMapper.toDto(entidade);

        assertThat(dto.codigo()).isEqualTo(CodigoTemplate.VEICULO_RETIRADO);
        assertThat(dto.conteudo()).isEqualTo("Conteudo DTO");
    }
}
