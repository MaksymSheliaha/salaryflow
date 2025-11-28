package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Role;
import com.msk.salaryflow.entity.User;
import com.msk.salaryflow.model.RestResponsePage;
import com.msk.salaryflow.model.UserListDto;
import com.msk.salaryflow.repository.RoleRepository;
import com.msk.salaryflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void saveUser_ShouldThrow_WhenUsernameAlreadyTakenByAnotherUser() {
        User existing = new User();
        existing.setId(UUID.randomUUID());
        existing.setUsername("user1");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user1");

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> userService.saveUser(user, "pass", "ROLE_USER"));
    }

    @Test
    void saveUser_ShouldThrow_WhenRoleNotFound() {
        User user = new User();
        user.setUsername("user2");

        when(userRepository.findByUsername("user2")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_MISSING")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.saveUser(user, null, "ROLE_MISSING"));
    }

    @Test
    void deleteById_ShouldDeleteWhenUserExists() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User deleted = userService.deleteById(id);

        assertEquals(user, deleted);
        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteById_ShouldReturnNullWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User deleted = userService.deleteById(id);

        assertNull(deleted);
        verify(userRepository, never()).deleteById(id);
    }

    @Test
    void findById_ShouldReturnUserOrNull() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        assertEquals(user, userService.findById(id));

        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertNull(userService.findById(id));
    }

    @Test
    void toggleStatus_ShouldDoNothingForAdminOrNull() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setEnabled(true);

        User resultAdmin = userService.toggleStatus(admin);
        assertTrue(resultAdmin.isEnabled());
        verify(userRepository, never()).save(admin);

        User resultNull = userService.toggleStatus(null);
        assertNull(resultNull);
    }

    @Test
    void findByUsername_ShouldReturnUserOrThrow() {
        User user = new User();
        user.setUsername("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        assertEquals(user, userService.findByUsername("john"));

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.findByUsername("john"));
    }

    @Test
    void changePassword_ShouldUpdatePassword_WhenOldPasswordMatches() {
        User user = new User();
        user.setUsername("john");
        user.setPassword("OLD_HASH");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "OLD_HASH")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("NEW_HASH");

        userService.changePassword("john", "old", "new");

        assertEquals("NEW_HASH", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_ShouldThrow_WhenOldPasswordIncorrect() {
        User user = new User();
        user.setUsername("john");
        user.setPassword("OLD_HASH");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "OLD_HASH")).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> userService.changePassword("john", "old", "new"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void findAll_ShouldUseNullSearchAndRoleFilter_WhenSearchAndRoleAreNullOrBlank() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("username"));
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("u1");
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());
        user.setRoles(Set.of());
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.filterUsers(any(), any(), any(), eq(pageable))).thenReturn(page);

        // searchTerm=null, role=null
        Page<UserListDto> result = userService.findAll(null, null, null, pageable);

        assertEquals(1, result.getTotalElements());

        ArgumentCaptor<String> searchCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> enabledCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(userRepository).filterUsers(searchCaptor.capture(), roleCaptor.capture(), enabledCaptor.capture(), eq(pageable));

        assertNull(searchCaptor.getValue());
        assertNull(roleCaptor.getValue());
        assertNull(enabledCaptor.getValue());
    }

    @Test
    void findAll_ShouldTrimAndLowercaseSearchTerm_AndIgnoreBlankRole() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("username"));
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("u2");
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());
        user.setRoles(Set.of());
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.filterUsers(any(), any(), any(), eq(pageable))).thenReturn(page);

        Page<UserListDto> result = userService.findAll("  TeSt  ", "   ", true, pageable);

        assertEquals(1, result.getTotalElements());

        ArgumentCaptor<String> searchCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> enabledCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(userRepository).filterUsers(searchCaptor.capture(), roleCaptor.capture(), enabledCaptor.capture(), eq(pageable));

        assertEquals("%test%", searchCaptor.getValue());
        assertNull(roleCaptor.getValue());
        assertEquals(Boolean.TRUE, enabledCaptor.getValue());
    }

    @Test
    void findAll_ShouldPassRoleFilter_WhenRoleIsNonBlank() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("username"));
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("u3");
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());
        user.setRoles(Set.of());
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.filterUsers(any(), any(), any(), eq(pageable))).thenReturn(page);

        Page<UserListDto> result = userService.findAll("user", "ROLE_ADMIN", false, pageable);

        assertEquals(1, result.getTotalElements());

        ArgumentCaptor<String> searchCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> enabledCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(userRepository).filterUsers(searchCaptor.capture(), roleCaptor.capture(), enabledCaptor.capture(), eq(pageable));

        assertEquals("%user%", searchCaptor.getValue());
        assertEquals("ROLE_ADMIN", roleCaptor.getValue());
        assertEquals(Boolean.FALSE, enabledCaptor.getValue());
    }
}