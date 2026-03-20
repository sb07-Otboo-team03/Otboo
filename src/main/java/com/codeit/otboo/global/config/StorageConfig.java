package com.codeit.otboo.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.nio.file.Path;

@Configuration
public class StorageConfig {

    // 로컬 파일 저장 시 사용할 루트 디렉토리 경로를 Bean으로 등록
    @Bean
    @ConditionalOnProperty(value = "otboo.storage.type", havingValue = "local")
    public Path localStoragePath(@Value("${otboo.storage.local.root-path}") String rootPath) {
        return Path.of(rootPath);
    }

    // S3 Presigned URL 생성을 위한 Presigner Bean 등록
    @Bean
    @ConditionalOnProperty(value = "otboo.storage.type", havingValue = "s3")
    public S3Presigner s3Presigner() {
        return S3Presigner.create();
    }

    // S3 버킷 이름을 설정값에서 가져와 다른 컴포넌트에서 사용할 수 있도록 Bean으로 등록
    @Bean
    @ConditionalOnProperty(value = "otboo.storage.type", havingValue = "s3")
    public String s3Bucket(@Value("${spring.cloud.aws.s3.bucket}") String bucket) {
        return bucket;
    }

    // S3에 업로드 및 조회 삭제 등을 위해 S3Client를 빈으로 등록
    @Bean
    @ConditionalOnProperty(value = "otboo.storage.type", havingValue = "s3")
    public S3Client s3Client() {
        return S3Client.create();
    }
}
