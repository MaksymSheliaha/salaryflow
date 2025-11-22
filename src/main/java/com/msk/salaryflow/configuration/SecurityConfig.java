package com.msk.salaryflow.configuration;

import com.msk.salaryflow.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Дозволяє використовувати анотації @PreAuthorize над методами
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Вимикаємо CSRF для простоти (на проді краще залишити)
                .authorizeHttpRequests(auth -> auth
                        // 1. Дозволяємо доступ до статичних ресурсів (CSS, JS) усім
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // 2. До сторінки створення користувачів (яку ми зробимо пізніше) - тільки АДМІН
                        .requestMatchers("/users/**").hasAuthority("ROLE_ADMIN")

                        // 3. Все інше вимагає авторизації (будь-яка роль)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // Використовуємо стандартну сторінку логіну від Spring Security
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/", true) // Після успішного входу - на Головну
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Використовуємо BCrypt для шифрування паролів
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}