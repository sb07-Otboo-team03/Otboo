package com.codeit.otboo.domain.binarycontent.controller;

import com.codeit.otboo.domain.binarycontent.controller.docs.BinaryContentControllerDocs;
import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.dto.response.BinaryContentInfoResponse;
import com.codeit.otboo.domain.binarycontent.mapper.BinaryContentMapper;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/binary-contents")
@RequiredArgsConstructor
public class BinaryContentController implements BinaryContentControllerDocs {
    private final BinaryContentService binaryContentService;
    private final BinaryContentMapper binaryContentMapper;

    // 파일 업로드
    @PostMapping
    public ResponseEntity<BinaryContentInfoResponse> upload(@RequestPart MultipartFile file) {
        BinaryContentCreateRequest req = binaryContentMapper.toRequestDto(file);
        BinaryContentInfoResponse binaryContent = binaryContentMapper.toResponseDto(binaryContentService.upload(req));
        return ResponseEntity.created(URI.create("/api/binary-contents/" + binaryContent.id()))
                .body(binaryContent);
    }

    // 파일 조회
    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        Resource file = binaryContentService.download(id);
        BinaryContentInfoResponse metadata = binaryContentMapper.toResponseDto(binaryContentService.getInfo(id));

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (metadata.type() != null && !metadata.type().isBlank()) {
            mediaType = MediaType.parseMediaType(metadata.type());
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getFilename() + "\""
                )
                .body(file);
    }

    // 파일 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        binaryContentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}