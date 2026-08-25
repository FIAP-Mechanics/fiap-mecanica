package com.fiap.mecanica.servico.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.servico.adapter.out.persistence.jpa.entity.ServicoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicoSpringDataRepository extends JpaRepository<ServicoJpaEntity, Long> {
}
