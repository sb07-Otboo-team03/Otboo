package com.codeit.otboo.domain.binarycontent.fixture;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

public class BinaryContentFixture {
    public static BinaryContent create() {
        BinaryContent newBinaryContent = new BinaryContent("test", "image/png", 64L);

        ReflectionTestUtils.setField(newBinaryContent, "id", UUID.randomUUID());
        return newBinaryContent;
    }

    public static BinaryContent create(
            String fileName, String fileType, long fileSize) {
        BinaryContent newBinaryContent = new BinaryContent(
                fileName, fileType, fileSize);

        ReflectionTestUtils.setField(newBinaryContent, "id", UUID.randomUUID());
        return newBinaryContent;
    }

    public static BinaryContent create(BinaryContentCreateRequest request) {
        BinaryContent newBinaryContent = new BinaryContent(
                request.name(), request.type(), request.size());
        ReflectionTestUtils.setField(newBinaryContent, "id", UUID.randomUUID());
        return newBinaryContent;
    }

    public static BinaryContent create(UUID binaryContentId){
        BinaryContent newBinaryContent = new BinaryContent("test", "image/png", 64L);
        ReflectionTestUtils.setField(newBinaryContent, "id", binaryContentId);
        return newBinaryContent;
    }
}
