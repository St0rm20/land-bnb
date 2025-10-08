package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.repository.ResetPasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenCleanupService {

    @Autowired
    private ResetPasswordRepository repository;

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void deleteExpiredTokens() {
        repository.deleteByExpirationTimeBefore(LocalDateTime.now());
    }
}

