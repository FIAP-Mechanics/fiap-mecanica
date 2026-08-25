package com.fiap.mecanica.estoque.adapter.in.web.error;

import java.util.List;

public record RespostaErro(
        List<ErroDetalhe> erros
) {
}
