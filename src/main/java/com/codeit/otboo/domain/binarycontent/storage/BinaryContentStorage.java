package com.codeit.otboo.domain.binarycontent.storage;

import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.UUID;

public interface BinaryContentStorage {

    UUID put(UUID binaryId, byte[] data);

    InputStream get(UUID binaryId);

    Resource download(UUID binaryId);

    void delete(UUID binaryId);
}
