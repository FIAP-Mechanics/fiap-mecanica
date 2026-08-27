package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity.TemplateNotificacaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemplateSpringDataRepository extends JpaRepository<TemplateNotificacaoJpaEntity, Long> {

    Optional<TemplateNotificacaoJpaEntity> findByCodigo(String codigo);
}
