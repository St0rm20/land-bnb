package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query(
            value = "SELECT a FROM User u JOIN u.favorites a WHERE u.id = :userId",
            countQuery = "SELECT COUNT(a) FROM User u JOIN u.favorites a WHERE u.id = :userId"
    )
    Page<Accommodation> findFavoritesByUserId(@Param("userId") Long userId, Pageable pageable);
}
