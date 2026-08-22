package com.fiap.mecanica.atendimento.repository;

import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateNotificacao, Long> {

    Optional<TemplateNotificacao> findByCodigo(String codigo);
}
