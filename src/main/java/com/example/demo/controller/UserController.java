package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, Model model) {
        User user = userService.findUserByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return "redirect:/home?id=" + user.getId();
        }
        model.addAttribute("error", "Invalid credentials");
        return "login";
    }

    @GetMapping("/home")
    public String home(@RequestParam Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "home";
    }

    @GetMapping("/change-password")
    public String changePassword() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String email, @RequestParam String oldPassword,
                                 @RequestParam String newPassword, Model model) {
        User user = userService.findUserByEmail(email);
        if (user != null && user.getPassword().equals(oldPassword)) {
            if (userService.isPasswordValid(newPassword)) {
                user.setPassword(newPassword);
                userService.saveUser(user);
                // Redirect to home with user ID
                return "redirect:/home?id=" + user.getId();
            }
            model.addAttribute("error", "New password does not meet the criteria");
        } else {
            model.addAttribute("error", "Invalid credentials");
        }
        return "change-password";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String emailOrPhone, Model model) {
        User user = userService.findUserByEmail(emailOrPhone);
        if (user == null) {
            user = userService.findUserByPhone(emailOrPhone);
        }
        if (user != null) {
            // Generate a reset token and save it (not shown here)
            String resetToken = "example-reset-token"; // Generate a unique token

            // Create a password reset link
            String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;

            // Send email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Password Reset Request");
            message.setText("To reset your password, click the link below:\n" + resetLink);
            mailSender.send(message);

            model.addAttribute("message", "A password reset link has been sent to your email.");
            return "forgot-password";
        } else {
            model.addAttribute("error", "No user found with that email or phone number.");
            return "forgot-password";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        return "login";
    }
}



