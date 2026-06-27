package com.fiap.mecanica.repository;

import com.fiap.mecanica.domain.OrdemServico;
import com.fiap.mecanica.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, String> {
    List<OrdemServico> findAllByStatusNot(Status status);
}
