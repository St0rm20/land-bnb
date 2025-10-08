package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.ResetPassword;
import com.labndbnb.landbnb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface ResetPasswordRepository extends JpaRepository<ResetPassword, Long> {
    ResetPassword findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM ResetPassword t WHERE t.expiresAt < :now")
    void deleteByExpirationTimeBefore(@Param("now") LocalDateTime now);

    ResetPassword findByUserId(Integer user_id);

    @Transactional
    void deleteAllByUserId(Integer user_id);

    ResetPassword getResetPasswordByUserAndExpiresAtBefore(User user, LocalDateTime now);


}
