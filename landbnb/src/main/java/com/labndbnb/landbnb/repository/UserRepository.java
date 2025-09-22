package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Buscar por email (útil para login/registro)
    Optional<User> findByEmail(String email);

    // Verificar si existe un usuario con ese email
    boolean existsByEmail(String email);
}
