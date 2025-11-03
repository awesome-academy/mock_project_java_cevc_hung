package com.mock_project_java_cevc_hung.hunglpmockjava.controller.admin;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.UserUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.UserResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.EmailAlreadyExistsException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    // Constants
    private static final String BASE_PATH = "/admin/users";
    private static final String VIEW_BASE = "admin/users/";
    private static final String VIEW_EDIT = VIEW_BASE + "edit";
    private static final String REDIRECT_USERS = "redirect:" + BASE_PATH;

    private final UserService userService;

    @Autowired
    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{id}/edit")
    public String editUserPage(@PathVariable Long id, Model model) {
        try {
            UserResponse user = userService.getUserById(id);
            UserUpdateRequest request = UserUpdateRequest.builder()
                    .name(user.getName())
                    .phone_number(user.getPhone_number())
                    .email(user.getEmail())
                    .address(user.getAddress())
                    .isActive(user.getIsActive())
                    .role(user.getRole())
                    .build();
            
            model.addAttribute("userUpdateRequest", request);
            model.addAttribute("userId", id);
            model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, "users");
            return VIEW_EDIT;
        } catch (ResourceNotFoundException e) {
            logger.error("User not found with id: {}", id, e);
            model.addAttribute(AdminConstants.ATTR_ERROR, "User not found");
            return REDIRECT_USERS;
        }
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(
            @PathVariable Long id,
            @Valid @ModelAttribute UserUpdateRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, "users");
            return VIEW_EDIT;
        }
        
        try {
            userService.updateUser(id, request);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, "User updated successfully!");
        } catch (EmailAlreadyExistsException e) {
            logger.warn("Email already exists: {}", request.getEmail());
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Email already exists: " + request.getEmail());
        } catch (ResourceNotFoundException e) {
            logger.error("User not found while updating id {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "User not found");
        } catch (Exception e) {
            logger.error("Error updating user id {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error updating user: " + e.getMessage());
        }
        return REDIRECT_USERS;
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, "User deleted successfully!");
        } catch (ResourceNotFoundException e) {
            logger.warn("User not found while deleting id {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "User not found");
        } catch (Exception e) {
            logger.error("Error deleting user id {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error deleting user: " + e.getMessage());
        }
        return REDIRECT_USERS;
    }
}

