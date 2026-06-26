package com.fiap.mecanica.dto;

import com.fiap.mecanica.infra.configs.enums.CodigoTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Dados do template de notificação")
public record TemplateDto(
        @Schema(description = "Código do template", example = "ORDEM_CRIADA") CodigoTemplate codigo,
        @Schema(description = "Conteúdo do template com placeholders", example = "Olá %s, sua ordem foi criada com sucesso!") String conteudo
) {
}
