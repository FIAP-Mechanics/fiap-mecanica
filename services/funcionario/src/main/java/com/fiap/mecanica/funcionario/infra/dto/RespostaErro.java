package com.fiap.mecanica.funcionario.infra.dto;

import java.util.List;

public record RespostaErro(
        List<ErroDetalhe> erros
) {
}
