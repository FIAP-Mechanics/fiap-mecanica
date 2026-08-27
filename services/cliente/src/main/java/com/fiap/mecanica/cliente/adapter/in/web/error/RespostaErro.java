package com.fiap.mecanica.cliente.adapter.in.web.error;

import java.util.List;

public record RespostaErro(
        List<ErroDetalhe> erros
) {
}
