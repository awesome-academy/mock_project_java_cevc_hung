package com.mock_project_java_cevc_hung.hunglpmockjava.controller;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.CategoryRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.TourRepository;
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
    private final TourRepository tourRepository;
    private final CategoryRepository categoryRepository;

    public AdminController(UserRepository userRepository, TourRepository tourRepository, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
        this.categoryRepository = categoryRepository;
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
        
        // count all entities
        long totalUsers = userRepository.count();
        long totalTours = tourRepository.count();
        long totalCategories = categoryRepository.count();
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalTours", totalTours);
        model.addAttribute("totalCategories", totalCategories);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        addAdminToModel(model);
        model.addAttribute("activePage", "users");
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

}
