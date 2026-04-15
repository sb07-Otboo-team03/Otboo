package com.codeit.otboo;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class OtbooApplication {
	public static void main(String[] args) {
		SpringApplication.run(OtbooApplication.class, args);
	}

    @PostConstruct
    public void started() {
        // 애플리케이션의 기본 타임존을 KST로 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
