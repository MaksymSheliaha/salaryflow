package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Role;
import com.msk.salaryflow.entity.User;
import com.msk.salaryflow.repository.RoleRepository;
import com.msk.salaryflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder; // Мокаємо шифратор

    @InjectMocks
    private UserService userService;

    @Test
    void saveUser_ShouldEncodePassword() {
        // ARRANGE
        User user = new User();
        user.setUsername("newadmin");
        String rawPassword = "secretPassword";

        // Налаштовуємо поведінку
        when(userRepository.findByUsername("newadmin")).thenReturn(Optional.empty()); // Юзера ще немає
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(new Role()));
        when(passwordEncoder.encode(rawPassword)).thenReturn("ENCODED_HASH"); // Імітуємо шифрування
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        User savedUser = userService.saveUser(user, rawPassword, "ROLE_ADMIN");

        // ASSERT
        assertNotNull(savedUser);
        assertEquals("ENCODED_HASH", savedUser.getPassword()); // Пароль має бути зашифрований!
        verify(passwordEncoder).encode(rawPassword); // Перевіряємо, що енкодер викликався
        verify(userRepository).save(user);
    }

    @Test
    void toggleStatus_ShouldFlipEnabled() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setUsername("manager");
        user.setEnabled(true); // Зараз активний

        // ACT (ми передаємо об'єкт напряму, як ти переробив у сервісі)
        userService.toggleStatus(user);

        // ASSERT
        assertFalse(user.isEnabled()); // Має стати неактивним (false)
        verify(userRepository).save(user);
    }
}