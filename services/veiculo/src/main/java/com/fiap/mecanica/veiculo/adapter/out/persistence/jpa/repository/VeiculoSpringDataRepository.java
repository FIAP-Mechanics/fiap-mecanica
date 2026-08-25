package com.fiap.mecanica.veiculo.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.veiculo.adapter.out.persistence.jpa.entity.VeiculoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeiculoSpringDataRepository extends JpaRepository<VeiculoJpaEntity, Long> {
    boolean existsByPlaca(String placa);

    Optional<VeiculoJpaEntity> findByPlaca(String placa);
}
