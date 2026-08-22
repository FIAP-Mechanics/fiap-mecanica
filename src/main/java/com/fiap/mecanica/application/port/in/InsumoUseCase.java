package com.fiap.mecanica.application.port.in;

import com.fiap.mecanica.domain.Insumo;

public interface InsumoUseCase {
    Insumo buscarInsumoPorId(Long id);
}
