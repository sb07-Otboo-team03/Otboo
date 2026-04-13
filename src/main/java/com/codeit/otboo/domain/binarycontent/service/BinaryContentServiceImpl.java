package com.codeit.otboo.domain.binarycontent.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentPresignedUrlRequest;
import com.codeit.otboo.domain.binarycontent.dto.response.BinaryContentPresignedUrlResponse;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.entity.UploadStatus;
import com.codeit.otboo.domain.binarycontent.event.BinaryContentDeletedEvent;
import com.codeit.otboo.domain.binarycontent.event.BinaryContentListDeletedEvent;
import com.codeit.otboo.domain.binarycontent.exception.BinaryContentNotFoundException;
import com.codeit.otboo.domain.binarycontent.exception.FileTypeNotSupportException;
import com.codeit.otboo.domain.binarycontent.exception.FileUploadMaximumSizeException;
import com.codeit.otboo.domain.binarycontent.presignedurl.BinaryContentPresignedUrlService;
import com.codeit.otboo.domain.binarycontent.repository.BinaryContentRepository;
import com.codeit.otboo.global.properties.MultipartProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BinaryContentServiceImpl implements BinaryContentService {
    private final ApplicationEventPublisher eventPublisher;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentPresignedUrlService binaryContentPresignedUrlService;
    private final MultipartProperties multipartProperties;

    @Override
    @Transactional
    public BinaryContentPresignedUrlResponse getPresignedUrl(BinaryContentPresignedUrlRequest request) {
        validateImageRequest(request);
        BinaryContent binaryContent = new BinaryContent(
                request.fileName(), request.contentType(), request.size());
        BinaryContent saved = binaryContentRepository.save(binaryContent);
        String uploadUrl = binaryContentPresignedUrlService.createPresignedUploadUrl(
                saved.getId(), request.contentType());
        return new BinaryContentPresignedUrlResponse(saved.getId(), uploadUrl);
    }

    @Override
    @Transactional
    public void completeUpload(UUID binaryContentId) {
        BinaryContent binaryContent = getById(binaryContentId);
        binaryContent.updateStatus(UploadStatus.SUCCESS);
    }

    @Override
    @Transactional
    public void delete(UUID binaryContentId) {
        BinaryContent binaryContent = getById(binaryContentId);
        binaryContentRepository.delete(binaryContent);
        eventPublisher.publishEvent(
                new BinaryContentDeletedEvent(binaryContentId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryContent getById(UUID id) {
        return binaryContentRepository.findById(id).orElseThrow(
                () -> new BinaryContentNotFoundException(id));
    }

    @Override
    public void deleteAllStaleProcessingBinaryContents(LocalDateTime cutoffTime) {
        List<UUID> targetIdList = binaryContentRepository.findAllByUploadStatusAndCreatedAtBefore(
                UploadStatus.PROCESSING, cutoffTime).stream().map(BinaryContent::getId).toList();
        if(targetIdList.isEmpty()) return;
        binaryContentRepository.deleteAllById(targetIdList);
        eventPublisher.publishEvent(new BinaryContentListDeletedEvent(targetIdList));
    }

    private void validateImageRequest(BinaryContentPresignedUrlRequest request) {
        long maxByteSize = multipartProperties.maxFileSize().toBytes();
        if(request.size() > maxByteSize){
            throw new FileUploadMaximumSizeException(request.size(), maxByteSize);
        }

        if (!request.contentType().startsWith("image/")) {
            throw new FileTypeNotSupportException(request.contentType(), "이미지파일");
        }
    }
}