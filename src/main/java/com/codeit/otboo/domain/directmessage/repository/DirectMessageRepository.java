package com.codeit.otboo.domain.directmessage.repository;

import com.codeit.otboo.domain.directmessage.dto.DirectMessageDto;
import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

    @Query("""
        SELECT new com.codeit.otboo.domain.directmessage.dto.DirectMessageDto(
            d.id,
            d.createdAt,
            sp.id,
            sp.name,
            spb.id,
            rp.id,
            rp.name,
            rpb.id,
            d.content)
        FROM DirectMessage d
            JOIN d.sender s
            JOIN d.receiver r
            JOIN s.profile sp
            JOIN r.profile rp
            LEFT JOIN sp.binaryContent spb
            LEFT JOIN rp.binaryContent rpb
        WHERE (d.sender.id = :userId
              OR d.receiver.id = :userId)
           AND ( CAST(:cursor AS timestamp) IS NULL
               OR (
                   d.createdAt < :cursor
                   OR (
                       d.createdAt = :cursor
                      AND (CAST(:idAfter AS uuid) IS NULL OR d.id < :idAfter)
                   )
               )
           )
        ORDER BY d.createdAt DESC, d.id DESC
    """)
    List<DirectMessageDto> findDirectMessageDtos(
        @Param("userId") UUID userId,
        @Param("cursor") LocalDateTime cursor,
        @Param("idAfter") UUID idAfter,
        Pageable pageable
    );
}
