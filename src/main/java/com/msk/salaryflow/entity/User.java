package com.msk.salaryflow.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // Імпортуємо UserDetails

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "users")
public class User implements UserDetails { // Імплементуємо UserDetails

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "enabled") // Виправляємо, щоб не було конфлікту з SQL-ключовими словами
    private boolean enabled = true;

    // Виправляємо назву колонки: SQL -> created_at, Java -> createdAt
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    // Зв'язок багато-до-багатьох з ролями
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            // Виправляємо назви колонок: SQL -> user_id
            joinColumns = @JoinColumn(name = "user_id"),
            // Виправляємо назви колонок: SQL -> role_id
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    // --- Обов'язкові методи інтерфейсу UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Рахунок не прострочений
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Рахунок не заблокований
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Облікові дані не прострочені
    }

    @Override
    public boolean isEnabled() {
        return this.enabled; // Використовуємо поле enabled з бази
    }
}