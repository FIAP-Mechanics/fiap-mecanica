package com.fiap.mecanica.atendimento.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Dados para atualização de template de notificação")
public record AtualizarTemplateRequest(
        @NotBlank @Schema(description = "Conteúdo do template com placeholders", example = "Olá %s, sua ordem foi atualizada!") String conteudo
) {
}
