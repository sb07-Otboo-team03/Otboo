package com.codeit.otboo.global.config;

import com.codeit.otboo.global.properties.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MultipartProperties.class)
public class MultipartConfig {
}
