package com.novasalud.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.novasalud.backend.entity.UsuarioRol;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol,Long>{
    List<UsuarioRol> findByUsuarioId(Long idUsuario);
    Optional<UsuarioRol> findByUsuarioIdAndRolId(Long idUsuario,Long idRol);
    List<UsuarioRol> findByRolId(Long idRol);
}
