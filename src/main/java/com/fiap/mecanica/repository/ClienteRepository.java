package com.fiap.mecanica.repository;

import com.fiap.mecanica.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByDocumento(String documento);

    Optional<Cliente> findByDocumento(String documento);

}
