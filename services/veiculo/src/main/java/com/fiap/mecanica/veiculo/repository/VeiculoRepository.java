package com.fiap.mecanica.veiculo.repository;

import com.fiap.mecanica.veiculo.domain.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    boolean existsByPlaca(String placa);

    Optional<Veiculo> findByPlaca(String placa);
}
