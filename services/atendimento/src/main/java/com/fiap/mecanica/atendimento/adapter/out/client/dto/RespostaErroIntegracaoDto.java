package com.fiap.mecanica.atendimento.adapter.out.client.dto;

import java.util.List;

public record RespostaErroIntegracaoDto(List<ErroIntegracaoDto> erros) {

    public record ErroIntegracaoDto(String codigo, String descricao) {
    }
}
