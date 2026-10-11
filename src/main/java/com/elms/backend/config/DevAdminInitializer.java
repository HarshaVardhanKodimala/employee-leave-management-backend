package com.elms.backend.config;

import com.elms.backend.entity.Role;
import com.elms.backend.entity.User;
import com.elms.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DevAdminInitializer {

    @Bean
    CommandLineRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (!userRepository.existsByEmail("admin@elms.com")) {

                User admin = new User();

                admin.setFullName("System Admin");
                admin.setEmail("admin@elms.com");
                admin.setPassword(
                        passwordEncoder.encode("Admin@123")
                );
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);

                System.out.println(
                        "========================================"
                );
                System.out.println(
                        "DEFAULT ADMIN CREATED"
                );
                System.out.println(
                        "Email: admin@elms.com"
                );
                System.out.println(
                        "Password: Admin@123"
                );
                System.out.println(
                        "========================================"
                );
            }
        };
    }
}