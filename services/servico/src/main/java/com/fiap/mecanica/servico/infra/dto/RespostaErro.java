package com.fiap.mecanica.servico.infra.dto;

import java.util.List;

public record RespostaErro(
        List<ErroDetalhe> erros
) {
}
