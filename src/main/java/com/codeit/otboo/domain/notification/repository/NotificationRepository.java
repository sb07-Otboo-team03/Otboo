package com.codeit.otboo.domain.notification.repository;

import com.codeit.otboo.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
