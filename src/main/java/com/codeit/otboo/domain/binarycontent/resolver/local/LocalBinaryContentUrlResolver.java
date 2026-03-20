package com.codeit.otboo.domain.binarycontent.resolver.local;

import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(value = "otboo.storage.type", havingValue = "local")
public class LocalBinaryContentUrlResolver implements BinaryContentUrlResolver {
    @Override
    public String resolve(UUID binaryContentId) {
        return "/api/binary-contents/" + binaryContentId;
    }
}
