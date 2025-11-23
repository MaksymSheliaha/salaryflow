package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Role;
import com.msk.salaryflow.entity.User;
import com.msk.salaryflow.repository.RoleRepository;
import com.msk.salaryflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
        String search = null;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            search = "%" + searchTerm.trim().toLowerCase() + "%";
        }
        String roleFilter = (role != null && !role.trim().isEmpty()) ? role : null;
        return userRepository.filterUsers(search, roleFilter, enabled, pageable);
    }

    // ВАЖЛИВО: Використовуємо @CacheEvict, щоб очистити старий запис з кешу при оновленні.
    // Ключ беремо з об'єкта user (#user.username)
    @CacheEvict(value = "users", key = "#user.username")
    public void saveUser(User user, String rawPassword, String roleName) {
        // 1. Перевірка на унікальність
        User existingUser = userRepository.findByUsername(user.getUsername()).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' is already taken.");
        }

        // 2. Хешування пароля
        if (rawPassword != null && !rawPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }

        // 3. Активація нового користувача
        if (user.getId() == null) {
            user.setEnabled(true);
        }

        // 4. Роль
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.setRoles(Set.of(role));
        userRepository.save(user);
    }

    public User findById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    // При видаленні - очищаємо весь кеш користувачів, бо ми не знаємо username по ID тут
    @CacheEvict(value = "users", allEntries = true)
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    // При зміні статусу - теж очищаємо все (простий варіант)
    @CacheEvict(value = "users", allEntries = true)
    public void toggleStatus(UUID id) {
        User user = findById(id);
        if (user != null && !user.getUsername().equals("admin")) {
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
        }
    }

    // ОСЬ ТУТ треба @Cacheable! Це метод, який викликається постійно при кожному запиті (Spring Security)
    @Cacheable(value = "users", key = "#username")
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // При зміні пароля - видаляємо старий запис з кешу
    @CacheEvict(value = "users", key = "#username")
    public void changePassword(String username, String oldPassword, String newPassword) {
        // Тут ми не викликаємо this.findByUsername, щоб не спрацював кеш всередині методу,
        // але для надійності краще дістати напряму з репозиторію
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}