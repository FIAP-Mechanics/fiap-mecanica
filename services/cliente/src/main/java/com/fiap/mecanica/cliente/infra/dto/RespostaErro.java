package com.fiap.mecanica.cliente.infra.dto;

import java.util.List;

public record RespostaErro(
        List<ErroDetalhe> erros
) {
}
