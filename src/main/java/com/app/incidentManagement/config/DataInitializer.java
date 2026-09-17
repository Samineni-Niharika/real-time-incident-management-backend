package com.app.incidentManagement.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.app.incidentManagement.entity.Role;
import com.app.incidentManagement.entity.User;
import com.app.incidentManagement.repository.UserRepository;
@Configuration
public class DataInitializer {
	 @Bean
	    CommandLineRunner createAdmin(
	            UserRepository userRepository,
	            PasswordEncoder passwordEncoder) {

	        return args -> {

	            if (userRepository.findByEmail("niharikasamineni983@gmail.com").isEmpty()) {

	                String adminPassword =
	                        System.getenv("ADMIN_PASSWORD");

	                if (adminPassword == null || adminPassword.isBlank()) {
	                    throw new IllegalStateException(
	                            "ADMIN_PASSWORD environment variable is not set"
	                    );
	                }

	                User admin = new User();

	                admin.setName("Niharika Samineni");
	                admin.setEmail("niharikasamineni983@gmail.com");

	                admin.setPassword(
	                        passwordEncoder.encode(adminPassword)
	                );

	                admin.setActive(true);
	                admin.setRole(Role.ADMIN);
	                admin.setCreatedAt(LocalDateTime.now());

	                userRepository.save(admin);

	                System.out.println("ADMIN user created successfully");
            }
        };
	}
}
