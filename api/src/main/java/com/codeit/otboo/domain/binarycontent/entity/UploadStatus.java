package com.codeit.otboo.domain.binarycontent.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UploadStatus {
    SUCCESS("성공"),
    PROCESSING("업로드중"),
    FAIL("실패");

    private final String status;
}