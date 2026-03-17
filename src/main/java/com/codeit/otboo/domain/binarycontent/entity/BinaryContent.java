package com.codeit.otboo.domain.binarycontent.entity;

import com.codeit.otboo.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "binary_contents")
@AllArgsConstructor
public class BinaryContent extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "size")
    private Integer size;

    @Column(name = "type")
    private String type;
}