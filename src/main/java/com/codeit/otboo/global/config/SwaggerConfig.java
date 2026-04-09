package com.codeit.otboo.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .components(new Components())
                ;
    }

    private Info apiInfo() {
        return new Info()
                .title("옷장을 부탁해 API")
                .description("옷장을 부탁해 서비스에 관련한 여러가지 기능을 제공하는 API 입니다.")
                .version("1.0.0")
                ;
    }
}
