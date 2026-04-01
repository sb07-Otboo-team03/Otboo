package com.codeit.otboo.domain.binarycontent.resolver.mock;

import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(value = "otboo.storage.type", havingValue = "mock")
@RequiredArgsConstructor
public class MockBinaryContentResolver implements BinaryContentUrlResolver {
    @Override
    public String resolve(UUID binaryContentId) {
        return "";
    }
}
