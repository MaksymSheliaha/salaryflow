package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Role;
import com.msk.salaryflow.entity.User;
import com.msk.salaryflow.repository.RoleRepository;
import com.msk.salaryflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<User> findAll(String searchTerm, String role, Boolean enabled, Pageable pageable) {
        // 1. Підготовка пошукового запиту
        String search = null;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            // Додаємо % і переводимо в нижній регістр тут, у Java
            search = "%" + searchTerm.trim().toLowerCase() + "%";
        }

        // 2. Обробка ролі
        String roleFilter = (role != null && !role.trim().isEmpty()) ? role : null;

        // 3. Виклик репозиторію
        return userRepository.filterUsers(search, roleFilter, enabled, pageable);
    }

    public void saveUser(User user, String rawPassword, String roleName) {
        // 1. ПЕРЕВІРКА НА УНІКАЛЬНІСТЬ
        // Шукаємо, чи є вже такий юзер
        User existingUser = userRepository.findByUsername(user.getUsername()).orElse(null);

        // Якщо такий юзер є, І це не редагування того самого юзера (id не співпадають)
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' is already taken.");
        }

        // ... далі ваш старий код ...
        if (rawPassword != null && !rawPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }

        if (user.getId() == null) {
            user.setEnabled(true);
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.setRoles(Set.of(role));
        userRepository.save(user);
    }

    public User findById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    // Метод для перемикання статусу (Active/Disabled)
    public void toggleStatus(UUID id) {
        User user = findById(id);
        if (user != null && !user.getUsername().equals("admin")) {
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
        }
    }
}