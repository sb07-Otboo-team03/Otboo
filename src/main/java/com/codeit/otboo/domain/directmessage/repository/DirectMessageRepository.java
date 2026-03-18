package com.codeit.otboo.domain.directmessage.repository;

import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.antlr.v4.runtime.atn.SemanticContext.OR;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

    @Query("""
    SELECT d FROM DirectMessage d
    WHERE d.receiver.id = :userId
    AND (
        :cursor IS NULL
        OR d.createdAt < :cursor
        OR (
            d.createdAt = :cursor
            AND (:idAfter IS NULL OR d.id < :idAfter)
        )
    )
    ORDER BY d.createdAt DESC, d.id DESC
""")
    Slice<DirectMessage> findDirectMessages(
        @Param("userId") UUID userId,
        @Param("cursor") LocalDateTime cursor,
        @Param("idAfter") UUID idAfter,
        Pageable pageable
    );
}
