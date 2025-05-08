package com.hepl.budgie;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FileService;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling
@EnableMethodSecurity
@EnableConfigurationProperties(StorageProperties.class)
@Slf4j
public class BudgieApplication {

	public static void main(String[] args) {
		SpringApplication.run(BudgieApplication.class, args);
	}

	@Bean
	CommandLineRunner init(FileService fileService) {
		return args -> fileService.init();
	}

}
