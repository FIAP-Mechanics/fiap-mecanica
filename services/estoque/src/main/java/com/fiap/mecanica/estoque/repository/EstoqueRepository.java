package com.fiap.mecanica.estoque.repository;

import com.fiap.mecanica.estoque.domain.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    List<Estoque> findAllByAtivoTrue();

    Optional<Estoque> findByInsumoId(Long idInsumo);
}
