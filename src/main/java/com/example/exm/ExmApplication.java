package com.example.exm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
//@EnableJpaAuditing
@EnableJpaRepositories
@EnableTransactionManagement
public class ExmApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExmApplication.class, args);
	}

	@Bean
	public FlywayMigrationStrategy flywayMigrationStrategy() {
		return flyway -> {
			// This will defer Flyway migration until after JPA is initialized
			// Do nothing here - migrations will be executed after EntityManagerFactory is ready
		};
	}

}
