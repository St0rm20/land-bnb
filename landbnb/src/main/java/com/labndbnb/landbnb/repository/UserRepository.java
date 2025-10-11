package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmailAndStatus(String email, UserStatus status);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    boolean existsByIdAndStatus(Long id, UserStatus status);


    Optional<User> findByIdAndStatus(Long id, UserStatus status);

    boolean existsByEmail(String email);

    User getUsersByIdAndStatus(Long id, UserStatus status);
}
