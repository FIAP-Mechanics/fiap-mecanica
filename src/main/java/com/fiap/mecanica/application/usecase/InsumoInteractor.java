package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.port.in.InsumoUseCase;
import com.fiap.mecanica.application.port.out.InsumoGateway;
import com.fiap.mecanica.domain.Insumo;
import com.fiap.mecanica.exception.InsumoNotFound;

public class InsumoInteractor implements InsumoUseCase {

    private final InsumoGateway insumoGateway;

    public InsumoInteractor(InsumoGateway insumoGateway) {
        this.insumoGateway = insumoGateway;
    }

    @Override
    public Insumo buscarInsumoPorId(Long id) {
        return insumoGateway.buscarPorId(id).orElseThrow(() -> new InsumoNotFound(id));
    }
}
