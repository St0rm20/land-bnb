package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmailAndStatus(String email, UserStatus status);

    boolean existsByEmail(String email);

    User getUsersById(Integer id);
}
