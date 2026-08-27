package com.fiap.mecanica.servico.adapter.in.web.error;

import java.util.List;

public record RespostaErro(
        List<ErroDetalhe> erros
) {
}
