package com.fiap.mecanica.veiculo.infra.dto;

import java.util.List;

public record RespostaErro(
        List<ErroDetalhe> erros
) {
}
