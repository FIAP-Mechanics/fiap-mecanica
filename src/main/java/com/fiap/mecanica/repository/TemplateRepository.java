package com.fiap.mecanica.repository;

import com.fiap.mecanica.domain.TemplateNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateNotificacao, Long> {

    Optional<TemplateNotificacao> findByCodigo(String codigo);
}
