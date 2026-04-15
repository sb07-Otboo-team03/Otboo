package com.codeit.otboo.batch;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

@SpringBootApplication(
        scanBasePackages = {
                "com.codeit.otboo.batch",
                "com.codeit.otboo.global",
                "com.codeit.otboo.domain.weather",
                "com.codeit.otboo.domain.notification"
        }
)
@EnableJpaRepositories(basePackages = {
        "com.codeit.otboo.domain.weather.repository"
})
@EntityScan(basePackages = {
        "com.codeit.otboo.domain.weather.entity",
        "com.codeit.otboo.domain.notification.entity"
})
public class OtbooBatchApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(OtbooBatchApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }

    @PostConstruct
    public void started() {
        // 애플리케이션의 기본 타임존을 KST로 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
