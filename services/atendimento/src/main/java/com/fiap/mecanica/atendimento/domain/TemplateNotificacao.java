package com.fiap.mecanica.atendimento.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateNotificacao {

    private Long id;

    private String codigo;

    private String conteudo;
}
