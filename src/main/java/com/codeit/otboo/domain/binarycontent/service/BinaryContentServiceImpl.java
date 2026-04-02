package com.codeit.otboo.domain.binarycontent.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.event.BinaryContentCreatedEvent;
import com.codeit.otboo.domain.binarycontent.event.BinaryContentDeletedEvent;
import com.codeit.otboo.domain.binarycontent.exception.BinaryContentNotFoundException;
import com.codeit.otboo.domain.binarycontent.exception.FileUploadMaximumSizeException;
import com.codeit.otboo.domain.binarycontent.repository.BinaryContentRepository;
import com.codeit.otboo.global.properties.MultipartProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BinaryContentServiceImpl implements BinaryContentService {
    private final ApplicationEventPublisher eventPublisher;
    private final BinaryContentRepository binaryContentRepository;
    private final MultipartProperties multipartProperties;

    @Override
    @Transactional
    public BinaryContent upload(BinaryContentCreateRequest request) {
        long maxByteSize = multipartProperties.maxFileSize().toBytes();
        if(request.size() > maxByteSize){
            throw new FileUploadMaximumSizeException(request.size(), maxByteSize);
        }
        BinaryContent binaryContent = new BinaryContent(request.name(), request.type(), request.size());
        BinaryContent saved = binaryContentRepository.save(binaryContent);
        eventPublisher.publishEvent(
                new BinaryContentCreatedEvent(binaryContent.getId(), request.data())
        );
        return saved;
    }

    @Override
    public void delete(UUID binaryContentId) {
        BinaryContent binaryContent = find(binaryContentId);
        binaryContentRepository.delete(binaryContent);
        eventPublisher.publishEvent(
                new BinaryContentDeletedEvent(binaryContentId)
        );
    }

    private BinaryContent find(UUID id) {
        return binaryContentRepository.findById(id).orElseThrow(
                () -> new BinaryContentNotFoundException(id)
        );
    }
}