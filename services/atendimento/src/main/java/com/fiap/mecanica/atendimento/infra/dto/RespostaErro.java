package com.fiap.mecanica.atendimento.infra.dto;

import java.util.List;

public record RespostaErro(
        List<ErroDetalhe> erros
) {
}
