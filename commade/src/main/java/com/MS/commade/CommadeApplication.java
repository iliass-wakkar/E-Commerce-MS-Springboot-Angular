package com.MS.commade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CommadeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommadeApplication.class, args);
	}

}
