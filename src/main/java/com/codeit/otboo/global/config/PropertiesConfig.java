package com.codeit.otboo.global.config;

import com.codeit.otboo.global.security.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class
})
public class PropertiesConfig {
}
