package com.codeit.otboo.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "servlet.multipart")
public record MultipartProperties(
        DataSize maxFileSize
){}
