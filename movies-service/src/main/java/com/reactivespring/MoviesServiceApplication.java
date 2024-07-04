package com.reactivespring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MoviesServiceApplication {

	public static void main(String[] args) {
		System.setProperty("os.arch", "x86_64");
		SpringApplication.run(MoviesServiceApplication.class, args);
	}

}
