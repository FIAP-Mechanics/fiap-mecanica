package com.fiap.mecanica.veiculo.adapter.in.web.error;

import java.util.List;

public record RespostaErro(
        List<ErroDetalhe> erros
) {
}
