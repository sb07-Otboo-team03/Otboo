package com.codeit.otboo.domain.notification.repository;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByIdAndReceiverId(UUID id, UUID receiverId);

    @Query("""
        SELECT new com.codeit.otboo.domain.notification.dto.NotificationDto(
            n.id,
            n.createdAt,
            n.receiver.id,
            n.title,
            n.content,
            n.level
        )
        FROM Notification n
        WHERE (
            n.receiver.id = :receiverId AND
            CAST(:cursor AS timestamp) IS NULL
                OR (
                    n.createdAt < :cursor
                    OR (
                        n.createdAt = :cursor
                            AND (CAST(:idAfter AS uuid) IS NULL OR n.id < :idAfter)
                    )
                )
            )
        ORDER BY n.createdAt DESC, n.id DESC
    """)
    List<NotificationDto> findAllByReceiverId(
        @Param("receiverId") UUID receiverId,
        @Param("cursor") LocalDateTime cursor,
        @Param("idAfter") UUID idAfter,
        Pageable pageable
    );
}
