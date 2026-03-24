package com.codeit.otboo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class OtbooApplication {

	public static void main(String[] args) {
		SpringApplication.run(OtbooApplication.class, args);
	}

}
