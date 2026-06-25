package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Insumo;
import com.fiap.mecanica.exception.InsumoNotFound;
import com.fiap.mecanica.repository.InsumoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class InsumoService {

    private final InsumoRepository insumoRepository;

    public Insumo buscarInsumoPorId(Long id) {
        return insumoRepository.findById(id)
                .orElseThrow(() -> new InsumoNotFound(id));
    }
}
