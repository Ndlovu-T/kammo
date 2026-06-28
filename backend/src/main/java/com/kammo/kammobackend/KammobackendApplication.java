package com.kammo.kammobackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class KammobackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(KammobackendApplication.class, args);
	}

}
