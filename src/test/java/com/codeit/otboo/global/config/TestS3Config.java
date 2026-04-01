package com.codeit.otboo.global.config;

import com.codeit.otboo.domain.binarycontent.storage.s3.S3BinaryContentStorage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestS3Config {

    @Bean
    public String s3Bucket() {
        return "bucket";
    }

    @Bean
    public S3BinaryContentStorage s3BinaryContentStorage() {
        return mock(S3BinaryContentStorage.class);
    }
}