package com.sebastiangelves.ss.repository;

import com.sebastiangelves.ss.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Spring Data JPA creará automáticamente la consulta para buscar un usuario por su username.
    Optional<Usuario> findByUsername(String username);
}