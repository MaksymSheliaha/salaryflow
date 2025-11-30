package com.msk.salaryflow.service;

import com.msk.salaryflow.aspect.annotation.LogEvent;
import com.msk.salaryflow.entity.Role;
import com.msk.salaryflow.entity.User;
import com.msk.salaryflow.model.RestResponsePage;
import com.msk.salaryflow.model.UserListDto;
import com.msk.salaryflow.repository.RoleRepository;
import com.msk.salaryflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Cacheable(value = "users_page_dto", key = "{#searchTerm, #role, #enabled, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString()}")
    public Page<UserListDto> findAll(String searchTerm, String role, Boolean enabled, Pageable pageable) {
        String search = null;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            search = "%" + searchTerm.trim().toLowerCase() + "%";
        }
        String roleFilter = (role != null && !role.trim().isEmpty()) ? role : null;

        Page<User> page = userRepository.filterUsers(search, roleFilter, enabled, pageable);

        List<UserListDto> dtos = page.getContent().stream()
                .map(UserListDto::new)
                .toList();

        return new RestResponsePage<>(dtos, page.getPageable(), page.getTotalElements());
    }

    @CacheEvict(value = "users_page_dto", allEntries = true)
    @LogEvent(action = "CREATE_USER")
    public User saveUser(User user, String rawPassword, String roleName) {
        User existingUser = userRepository.findByUsername(user.getUsername()).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' is already taken.");
        }
        if (rawPassword != null && !rawPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        if (user.getId() == null) {
            user.setEnabled(true);
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.setRoles(Set.of(role));

        return userRepository.save(user);
    }

    public User findById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    @LogEvent(action="DELETE_USER")
    @CacheEvict(value = "users_page_dto", allEntries = true)
    public User deleteById(UUID id) {
        User userToDelete = userRepository.findById(id).orElse(null);
        if (userToDelete != null) {
            userRepository.deleteById(id);
        }
        return userToDelete;
    }


    @CacheEvict(value = "users_page_dto", allEntries = true)
    @LogEvent(action = "UPDATE_USER_STATUS")
    public User toggleStatus(User user) {
        if (user != null && !user.getUsername().equals("admin")) {
            user.setEnabled(!user.isEnabled());
            return userRepository.save(user);
        }
        return user;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @CacheEvict(value = "users_page_dto", allEntries = true)
    @LogEvent(action = "CHANGE_PASSWORD")
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Incorrect old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}