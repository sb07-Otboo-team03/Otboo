package com.codeit.otboo.domain.binarycontent.repository;

import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.entity.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BinaryContentRepository extends JpaRepository<BinaryContent, UUID> {
    List<BinaryContent> findAllByUploadStatusAndCreatedAtBefore(
            UploadStatus uploadStatus, LocalDateTime createdAt);
}
