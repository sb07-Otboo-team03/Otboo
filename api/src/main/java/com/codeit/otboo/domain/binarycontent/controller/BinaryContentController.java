package com.codeit.otboo.domain.binarycontent.controller;

import com.codeit.otboo.domain.binarycontent.controller.docs.BinaryContentControllerDocs;
import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentPresignedUrlRequest;
import com.codeit.otboo.domain.binarycontent.dto.response.BinaryContentPresignedUrlResponse;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/binary-contents")
@RequiredArgsConstructor
public class BinaryContentController implements BinaryContentControllerDocs {
    private final BinaryContentService binaryContentService;

    @PostMapping("/images/presigned-url")
    public ResponseEntity<BinaryContentPresignedUrlResponse> issueImagePresignedUrl(
            @Valid @RequestBody BinaryContentPresignedUrlRequest request
    ){
        BinaryContentPresignedUrlResponse response =
                binaryContentService.getPresignedUrl(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{binaryContentId}/complete")
    public ResponseEntity<Void> completeUpload(@PathVariable UUID binaryContentId) {
        binaryContentService.completeUpload(binaryContentId);
        return ResponseEntity.ok().build();
    }
}
