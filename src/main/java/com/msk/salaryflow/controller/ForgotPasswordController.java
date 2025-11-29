package com.msk.salaryflow.controller;

import com.msk.salaryflow.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final PasswordResetService resetService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("username") String username, RedirectAttributes attributes) {
        resetService.createTokenAndSendEmail(username);
        attributes.addFlashAttribute("message", "If user exists, reset link has been sent (Check Console!)");
        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        String result = resetService.validateToken(token);
        if (result != null) {
            model.addAttribute("error", result);
            return "login"; // Або сторінка помилки
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       RedirectAttributes attributes) {
        resetService.resetPassword(token, password);
        attributes.addFlashAttribute("message", "Password successfully reset. Please login.");
        return "redirect:/login";
    }
}