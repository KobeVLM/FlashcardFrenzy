package com.marikit.flashcardfrenzy.common;

import com.marikit.flashcardfrenzy.auth.User;
import com.marikit.flashcardfrenzy.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default admin user if no admin exists in the database.
 * Credentials: admin@flashcardfrenzy.com / Admin123!
 */
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Check if ANY admin exists (safer than checking just one email)
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(u -> u.getRole() == User.Role.ADMIN);

        if (!adminExists) {
            User admin = User.builder()
                    .fullName("Platform Administrator")
                    .email("admin@flashcardfrenzy.com")
                    .passwordHash(passwordEncoder.encode("Admin123!"))
                    .role(User.Role.ADMIN)
                    .build();
            
            userRepository.save(admin);
            System.out.println("====================================================");
            System.out.println("DEFAULT ADMIN CREATED");
            System.out.println("Email: admin@flashcardfrenzy.com");
            System.out.println("Password: Admin123!");
            System.out.println("====================================================");
        }
    }
}
