package com.fiap.mecanica.adapter.in.web.request;

import com.fiap.mecanica.domain.CodigoTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "Dados para cadastro de template de notificação")
public record CriarTemplateRequest(
        @NotNull @Schema(description = "Código único do template", example = "ORDEM_CRIADA") CodigoTemplate codigo,
        @NotBlank @Schema(description = "Conteúdo do template com placeholders", example = "Olá %s, sua ordem foi criada com sucesso!") String conteudo) {
}
