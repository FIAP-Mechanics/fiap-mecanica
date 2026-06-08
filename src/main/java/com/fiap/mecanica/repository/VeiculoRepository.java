package com.fiap.mecanica.repository;

import com.fiap.mecanica.domain.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
	boolean existsByPlaca(String placa);
}