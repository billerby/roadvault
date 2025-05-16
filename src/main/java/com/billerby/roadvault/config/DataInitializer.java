package com.billerby.roadvault.config;

import com.billerby.roadvault.model.User;
import com.billerby.roadvault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Initialize data on application startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setEnabled(true);
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.addRole("ROLE_ADMIN");
            adminUser.addRole("ROLE_USER");
            
            userRepository.save(adminUser);
            
            System.out.println("Created admin user with username 'admin' and password 'admin'");
        }
    }
}
