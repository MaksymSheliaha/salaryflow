package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.PasswordResetToken;
import com.msk.salaryflow.entity.User;
import com.msk.salaryflow.repository.PasswordResetTokenRepository;
import com.msk.salaryflow.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createTokenAndSendEmail(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return; // Якщо юзера немає, мовчимо з міркувань безпеки

        // Видаляємо старі токени, якщо були
        tokenRepository.deleteByUser(user);

        // Створюємо новий токен
        String token = UUID.randomUUID().toString();
        PasswordResetToken myToken = new PasswordResetToken(user, token);
        tokenRepository.save(myToken);

        // ТУТ МАЛА БУТИ ВІДПРАВКА EMAIL
        // Але ми виведемо в консоль для тесту
        System.out.println("========================================");
        System.out.println("PASSWORD RESET LINK for user: " + username);
        System.out.println("http://localhost:8080/reset-password?token=" + token);
        System.out.println("========================================");
    }

    public String validateToken(String token) {
        Optional<PasswordResetToken> passToken = tokenRepository.findByToken(token);

        if (passToken.isEmpty()) {
            return "Invalid token";
        }
        if (passToken.get().isExpired()) {
            return "Token expired";
        }
        return null; // Все ок
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken passToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        User user = passToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Видаляємо використаний токен
        tokenRepository.delete(passToken);
    }
}