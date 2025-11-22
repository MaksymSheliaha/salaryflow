package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.User;
import com.msk.salaryflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    // Відкрити сторінку профілю
    @GetMapping
    public String profile(Model model, Principal principal) {
        String username = principal.getName();
        // Нам треба додати метод findByUsername у UserService, якщо його немає (див. нижче)
        // Якщо у тебе в UserService немає методу findByUsername, я напишу його в кроці 4
        User user = userService.findById(userService.findByUsername(username).getId());

        model.addAttribute("user", user);
        return "profile";
    }

    // Обробка зміни пароля
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match!");
            return "redirect:/profile";
        }

        try {
            userService.changePassword(principal.getName(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }
}