package com.fiap.mecanica.atendimento.repository;

import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, String> {
    List<OrdemServico> findAllByStatusNotIn(List<Status> status);

    List<OrdemServico> findAllByStatusIn(List<Status> status);
}
