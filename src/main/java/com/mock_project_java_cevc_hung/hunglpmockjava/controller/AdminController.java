package com.mock_project_java_cevc_hung.hunglpmockjava.controller;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    private void addAdminToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        UserEntity admin = userRepository.findByEmail(email).orElse(null);
        
        if (admin != null) {
            model.addAttribute("admin", admin);
        } else {
            UserEntity defaultAdmin = new UserEntity();
            defaultAdmin.setName("Administrator");
            defaultAdmin.setEmail(email);
            model.addAttribute("admin", defaultAdmin);
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("error", "Invalid username or password");
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        addAdminToModel(model);
        model.addAttribute("activePage", "dashboard");
        
        // count all user
        long totalUsers = userRepository.count();
        model.addAttribute("totalUsers", totalUsers);
        
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        addAdminToModel(model);
        model.addAttribute("activePage", "users");
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @GetMapping("/tours")
    public String tours(Model model) {
        addAdminToModel(model);
        model.addAttribute("activePage", "tours");
        // TODO: Add tour repository and get tours
        return "admin/tours";
    }
}
