package com.msk.salaryflow.configuration;

import com.msk.salaryflow.entity.Role;
import com.msk.salaryflow.entity.User;
import com.msk.salaryflow.repository.RoleRepository;
import com.msk.salaryflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createOrUpdateAdmin();
    }
    private void createOrUpdateAdmin() {
        String username = "admin";
        if (userRepository.findByUsername(username).isPresent()) {
            System.out.println(">>> Admin user already exists. Skipping initialization.");
            return;
        }
        System.out.println(">>> Creating System Admin...");

        User admin = new User();
        admin.setUsername(username);

        String rawPassword = "admin123";
        admin.setPassword(passwordEncoder.encode(rawPassword));

        admin.setEnabled(true);

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_ADMIN is not found."));

        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);
        System.out.println(">>> Admin created successfully with default password.");
    }
}