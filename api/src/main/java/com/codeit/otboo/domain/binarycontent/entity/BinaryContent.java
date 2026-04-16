package com.codeit.otboo.domain.binarycontent.entity;

import com.codeit.otboo.domain.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "binary_contents")
public class BinaryContent extends BaseUpdatableEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "size")
    private Long size;

    @Column(name = "upload_status")
    @Enumerated(EnumType.STRING)
    private UploadStatus uploadStatus;

    public BinaryContent(String name, String type, Long size){
        this.name = name;
        this.type = type;
        this.size = size;
        this.uploadStatus = UploadStatus.PROCESSING;
    }

    public void updateStatus(UploadStatus uploadStatus){
        this.uploadStatus = uploadStatus;
    }
}



