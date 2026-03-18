package com.codeit.otboo.domain.binarycontent.storage.local;

import com.codeit.otboo.domain.binarycontent.storage.BinaryContentStorage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
@ConditionalOnProperty(value = "otboo.storage.type", havingValue = "local")
@RequiredArgsConstructor
public class LocalBinaryContentStorage implements BinaryContentStorage {

    private final Path root;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new IllegalStateException("로컬 storage 루트 경로 생성 실패: " + root, e);
        }
    }

    // 저장
    @Override
    public UUID put(UUID binaryId, byte[] data) {
        if (binaryId == null) {
            throw new NullPointerException("binaryId는 null일 수 없습니다");
        }
        try {
            Path filePath = resolvePath(binaryId);
            Files.write(filePath, data);
            return binaryId;
        } catch (IOException e) {
            throw new UncheckedIOException("파일 저장 실패: " + binaryId, e);
        }
    }

    // 조회
    @Override
    public InputStream get(UUID binaryId) {
        try {
            Path filePath = resolvePath(binaryId);
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("파일 읽기 실패: " + binaryId, e);
        }
    }

    // 다운로드
    @Override
    public Resource download(UUID binaryContentId) {
        try {
            Path filePath = resolvePath(binaryContentId);
            Files.newInputStream(filePath).close(); // 존재 확인
            return new FileSystemResource(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("파일 다운로드 실패: " + binaryContentId, e);
        }
    }

    // 삭제
    @Override
    public void delete(UUID binaryId) {
        try {
            Path filePath = resolvePath(binaryId);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("파일 삭제 실패: " + binaryId, e);
        }
    }

    // 내부 유틸: UUID → Path
    private Path resolvePath(UUID binaryId) {
        return root.resolve(binaryId.toString());
    }
}
