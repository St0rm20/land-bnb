package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
