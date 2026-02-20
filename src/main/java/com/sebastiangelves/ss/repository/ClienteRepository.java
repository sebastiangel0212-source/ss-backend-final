package com.sebastiangelves.ss.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sebastiangelves.ss.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCedula(String cedula);
}