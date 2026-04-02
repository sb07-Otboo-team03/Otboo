package com.codeit.otboo.domain.notification.repository;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByIdAndReceiver_Id(UUID id, UUID receiverId);

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
    List<NotificationDto> findAll(
        @Param("receiverId") UUID receiverId,
        @Param("cursor") LocalDateTime cursor,
        @Param("idAfter") UUID idAfter,
        Pageable pageable
    );

//    @Modifying(clearAutomatically = true)
//    @Query("""
//        INSERT INTO Notification (title, content, level, receiver, batchId)
//        SELECT :title, :content, :level, u, :batchId
//        FROM User u
//        WHERE u.locked = true
//    """)
//    int insertNotificationsToLockedUsers(
//        @Param("title") String title,
//        @Param("content") String content,
//        @Param("level") NotificationLevel level,
//        @Param("batchId") UUID batchId
//    );

}
