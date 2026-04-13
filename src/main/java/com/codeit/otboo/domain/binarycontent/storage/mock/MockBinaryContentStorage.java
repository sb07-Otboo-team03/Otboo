package com.codeit.otboo.domain.binarycontent.storage.mock;

import com.codeit.otboo.domain.binarycontent.storage.BinaryContentStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.UUID;

@Component
@ConditionalOnProperty(value = "otboo.storage.type", havingValue = "mock")
public class MockBinaryContentStorage implements BinaryContentStorage {
    @Override
    public InputStream get(UUID binaryId) {
        return null;
    }

    @Override
    public Resource download(UUID binaryId) {
        return null;
    }

    @Override
    public void delete(UUID binaryId) {

    }
}
