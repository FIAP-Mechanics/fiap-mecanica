package com.fiap.mecanica.controller.mapper;

import com.fiap.mecanica.controller.request.CriarTemplateRequest;
import com.fiap.mecanica.domain.TemplateNotificacao;
import com.fiap.mecanica.dto.TemplateDto;
import com.fiap.mecanica.infra.configs.enums.CodigoTemplate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateMapperTest {

    @Test
    void deveConverterRequestParaEntidade() {
        // Arrange
        CriarTemplateRequest request = CriarTemplateRequest.builder()
                .codigo(CodigoTemplate.RETIRAR_VEICULO)
                .conteudo("Conteudo teste")
                .build();

        // Act
        TemplateNotificacao entidade = TemplateMapper.toEntity(request);

        // Assert
        assertThat(entidade.getCodigo()).isEqualTo(CodigoTemplate.RETIRAR_VEICULO.name());
        assertThat(entidade.getConteudo()).isEqualTo("Conteudo teste");
    }

    @Test
    void deveConverterEntidadeParaDto() {
        // Arrange
        TemplateNotificacao entidade = TemplateNotificacao.builder()
                .codigo(CodigoTemplate.VEICULO_RETIRADO.name())
                .conteudo("Conteudo DTO")
                .build();

        // Act
        TemplateDto dto = TemplateMapper.toDto(entidade);

        // Assert
        assertThat(dto.codigo()).isEqualTo(CodigoTemplate.VEICULO_RETIRADO);
        assertThat(dto.conteudo()).isEqualTo("Conteudo DTO");
    }
}
