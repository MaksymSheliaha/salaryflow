package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.User;
import com.msk.salaryflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        // ARRANGE
        String username = "admin";
        User user = new User();
        user.setUsername(username);
        user.setPassword("hash");
        user.setRoles(Set.of()); // Пусті ролі, щоб не було NullPointerException

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // ACT
        UserDetails result = userDetailsService.loadUserByUsername(username);

        // ASSERT
        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void loadUserByUsername_WhenUserNotFound_ShouldThrowException() {
        // ARRANGE
        String username = "ghost";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(username);
        });
    }
}