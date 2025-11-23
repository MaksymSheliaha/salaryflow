package com.msk.salaryflow.configuration;

import com.msk.salaryflow.entity.Absence;
import com.msk.salaryflow.entity.AbsenceType;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.entity.Role;
import com.msk.salaryflow.entity.User;
import com.msk.salaryflow.repository.AbsenceRepository;
import com.msk.salaryflow.repository.EmployeeRepository;
import com.msk.salaryflow.repository.RoleRepository;
import com.msk.salaryflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final AbsenceRepository absenceRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createOrUpdateAdmin();
        generateTestAbsences();
    }

//    private void createOrUpdateAdmin() {
//        String username = "admin";
//        String rawPassword = "admin123";
//        User admin = userRepository.findByUsername(username).orElse(new User());
//        admin.setUsername(username);
//        admin.setPassword(passwordEncoder.encode(rawPassword));
//        admin.setEnabled(true);
//        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
//                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_ADMIN is not found in DB."));
//
//        admin.setRoles(Set.of(adminRole));
//
//        userRepository.save(admin);
//
//        System.out.println("----------------------------------------------------------");
//        System.out.println(">>> ADMIN USER UPDATED SUCCESSFULLY!");
//        System.out.println(">>> Login: " + username);
//        System.out.println(">>> Pass : " + rawPassword);
//        System.out.println("----------------------------------------------------------");
//    }

//    private void createOrUpdateAdmin() {
//        String username = "admin";
//        String rawPassword = "admin123";
//
//        // Шукаємо адміна в базі
//        User admin = userRepository.findByUsername(username).orElse(null);
//
//        if (admin == null) {
//            admin = new User();
//            admin.setUsername(username);
//            admin.setPassword(passwordEncoder.encode(rawPassword));
//            admin.setEnabled(true);
//
//            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
//                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_ADMIN is not found."));
//            admin.setRoles(Set.of(adminRole));
//
//            userRepository.save(admin);
//            System.out.println(">>> Admin CREATED");
//        } else {
//            boolean passwordMatches = passwordEncoder.matches(rawPassword, admin.getPassword());
//
//            if (!passwordMatches) {
//                admin.setPassword(passwordEncoder.encode(rawPassword));
//                userRepository.save(admin);
//                System.out.println(">>> Admin password UPDATED (it was different)");
//            } else {
//                System.out.println(">>> Admin already exists and password is correct. No changes.");
//            }
//        }
//    }
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
    private void generateTestAbsences() {
        if (absenceRepository.count() > 0) return;

        System.out.println(">>> Generating test data for MongoDB (Absences)...");
        List<Employee> employees = employeeRepository.findAll();
        if (employees.isEmpty()) return;

        Random random = new Random();
        AbsenceType[] types = AbsenceType.values();
        int count = 0;

        for (Employee emp : employees) {
            if (count++ > 50) break;
            int absencesToGenerate = random.nextInt(3) + 1;

            for (int i = 0; i < absencesToGenerate; i++) {
                Absence absence = new Absence();
                absence.setId(UUID.randomUUID());
                absence.setEmployeeId(emp.getId().toString());
                absence.setType(types[random.nextInt(types.length)]);

                int randomDay = random.nextInt(365);
                LocalDate start = LocalDate.of(2024, 1, 1).plusDays(randomDay);
                absence.setStartDate(start);
                absence.setEndDate(start.plusDays(random.nextInt(10) + 1));
                absence.setComment("Auto-generated test absence");

                absenceRepository.save(absence);
            }
        }
        System.out.println(">>> Absences generated.");
    }
}