package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmailAndStatus(String email, UserStatus status);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    boolean existsByIdAndStatus(Integer id, UserStatus status);


    Optional<User> findByIdAndStatus(Integer id, UserStatus status);

    boolean existsByEmail(String email);

    User getUsersByIdAndStatus(Integer id, UserStatus status);
}
