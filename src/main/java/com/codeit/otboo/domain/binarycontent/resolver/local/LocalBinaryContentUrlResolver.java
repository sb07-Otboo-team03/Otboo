package com.codeit.otboo.domain.binarycontent.resolver.local;

import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LocalBinaryContentUrlResolver implements BinaryContentUrlResolver {
    @Override
    public String resolve(UUID binaryContentId) {
        return "/api/binary-contents/" + binaryContentId;
    }
}
