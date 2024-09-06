package com.example.JAVARest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication

public class JavaRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaRestApplication.class, args);
	}

}
