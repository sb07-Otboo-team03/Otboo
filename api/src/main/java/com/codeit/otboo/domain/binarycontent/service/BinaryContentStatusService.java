package com.codeit.otboo.domain.binarycontent.service;

import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.entity.UploadStatus;
import com.codeit.otboo.domain.binarycontent.exception.BinaryContentNotFoundException;
import com.codeit.otboo.domain.binarycontent.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BinaryContentStatusService {
    private final BinaryContentRepository binaryContentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSuccess(UUID id) {
        BinaryContent binaryContent = binaryContentRepository.findById(id).orElseThrow(
                () -> new BinaryContentNotFoundException(id));
        binaryContent.updateStatus(UploadStatus.SUCCESS);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFail(UUID id) {
        BinaryContent binaryContent = binaryContentRepository.findById(id).orElseThrow(
                () -> new BinaryContentNotFoundException(id));
        binaryContent.updateStatus(UploadStatus.FAIL);
    }
}
