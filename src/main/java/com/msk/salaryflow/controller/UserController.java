package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.User;
import com.msk.salaryflow.model.UserListDto;
import com.msk.salaryflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(Model model,
                            @RequestParam(value = "q", required = false) String searchTerm,
                            @RequestParam(value = "role", required = false) String role,
                            @RequestParam(value = "status", required = false) Boolean status,
                            @PageableDefault(sort = "username", direction = Sort.Direction.ASC, size = 10) Pageable pageable) {

        Page<UserListDto> pageFromService = userService.findAll(searchTerm, role, status, pageable);
        Page<UserListDto> uiPage = new org.springframework.data.domain.PageImpl<>(
                pageFromService.getContent(),
                pageable,
                pageFromService.getTotalElements()
        );

        model.addAttribute("users", uiPage.getContent());
        model.addAttribute("page", uiPage); // Передаємо "виправлену" сторінку

        model.addAttribute("currentSearch", searchTerm);
        model.addAttribute("currentRole", role);
        model.addAttribute("currentStatus", status);

        return "users/user-list";
    }

    @GetMapping("/add")
    public String addUserForm(Model model) {
        model.addAttribute("user", new User());
        return "users/user-form";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute User user,
                           @RequestParam(required = false) String rawPassword,
                           @RequestParam("role") String roleName,
                           Model model) { // <--- Додали Model
        try {
            userService.saveUser(user, rawPassword, roleName);
            return "redirect:/users";
        } catch (IllegalArgumentException e) {
            // Якщо зловили помилку (наприклад, дублікат імені)
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user); // Повертаємо введені дані, щоб не зникли
            return "users/user-form"; // Повертаємо на сторінку форми, а не редірект
        }
    }

    @GetMapping("/delete")
    public String deleteUser(@RequestParam("id") UUID id) {
        userService.deleteById(id);
        return "redirect:/users";
    }

    @GetMapping("/toggle")
    public String toggleStatus(@RequestParam("id") UUID id) {
        userService.toggleStatus(id);
        return "redirect:/users";
    }
}