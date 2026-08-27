package com.fiap.mecanica.funcionario.adapter.in.web.error;

import java.util.List;

public record RespostaErro(
        List<ErroDetalhe> erros
) {
}
