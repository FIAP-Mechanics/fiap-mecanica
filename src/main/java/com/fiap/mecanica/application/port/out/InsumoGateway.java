package com.fiap.mecanica.application.port.out;

import com.fiap.mecanica.domain.Insumo;

import java.util.Optional;

public interface InsumoGateway {
    Optional<Insumo> buscarPorId(Long id);
}
