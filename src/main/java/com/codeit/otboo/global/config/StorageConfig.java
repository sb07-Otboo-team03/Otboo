package com.codeit.otboo.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(value = "otboo.storage.type", havingValue = "local")
    public Path localStoragePath(@Value("${otboo.storage.local.root-path}") String rootPath) {
        return Path.of(rootPath);
    }
}
