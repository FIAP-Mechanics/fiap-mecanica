package com.fiap.mecanica.infra.configs.dto;

import java.util.List;

public record RespostaErro(
        List<ErroDetalhe> erros
) {
}