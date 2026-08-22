package com.fiap.mecanica.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.VeiculoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoSpringDataRepository extends JpaRepository<VeiculoJpaEntity, Long> {
    boolean existsByPlaca(String placa);
}
