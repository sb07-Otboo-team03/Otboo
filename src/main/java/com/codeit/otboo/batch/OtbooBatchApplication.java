package com.codeit.otboo.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OtbooBatchApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(OtbooBatchApplication.class);
        app.setAdditionalProfiles("batch");
        app.run(args);
    }
}
