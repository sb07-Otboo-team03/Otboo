package com.codeit.otboo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseUpdatableEntity extends BaseEntity {

    @LastModifiedDate
    @Column(nullable = true)
    private LocalDateTime updatedAt;

    protected void touch(){
        this.updatedAt = LocalDateTime.now();
    }
}
