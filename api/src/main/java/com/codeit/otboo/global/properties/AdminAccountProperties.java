package com.codeit.otboo.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otboo.account")
public record AdminAccountProperties(
        String email,
        String name,
        String password
) {

}
