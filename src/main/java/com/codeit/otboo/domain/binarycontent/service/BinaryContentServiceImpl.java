package com.codeit.otboo.domain.binarycontent.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.exception.BinaryContentNotFoundException;
import com.codeit.otboo.domain.binarycontent.exception.FileUploadMaximumSizeException;
import com.codeit.otboo.domain.binarycontent.repository.BinaryContentRepository;
import com.codeit.otboo.domain.binarycontent.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BinaryContentServiceImpl implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;

    @Value("${servlet.multipart.maxFileSize}")
    private DataSize maxFileSize;

    @Override
    @Transactional
    public BinaryContent upload(BinaryContentCreateRequest request) {
        long maxByteSize = maxFileSize.toBytes();
        if(request.size() > maxByteSize){
            throw new FileUploadMaximumSizeException(request.size(), maxByteSize);
        }
        BinaryContent binaryContent = new BinaryContent(request.name(), request.type(), request.size());
        BinaryContent infoSaved = binaryContentRepository.save(binaryContent);
        binaryContentStorage.put(infoSaved.getId(), request.data());
        return infoSaved;
    }

    @Override
    @Transactional(readOnly = true)
    public Resource download(UUID binaryContentId) {
        find(binaryContentId);
        return binaryContentStorage.download(binaryContentId);
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryContent getInfo(UUID binaryContentId) {
        return find(binaryContentId);
    }

    @Override
    public void delete(UUID binaryContentId) {
        BinaryContent binaryContent = find(binaryContentId);
        binaryContentStorage.delete(binaryContentId);
        binaryContentRepository.delete(binaryContent);
    }

    private BinaryContent find(UUID id) {
        return binaryContentRepository.findById(id).orElseThrow(
                () -> new BinaryContentNotFoundException(id)
        );
    }
}