package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.ResetPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResetPasswordRepository extends JpaRepository<ResetPassword, Long> {
}
