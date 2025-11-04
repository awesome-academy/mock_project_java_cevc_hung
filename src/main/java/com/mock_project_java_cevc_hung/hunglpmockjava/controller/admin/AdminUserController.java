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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    private final UserService userService;
    private final MessageSource messageSource;

    @Autowired
    public AdminUserController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }
    
    private void addErrorFlash(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, message);
    }
    
    private void addSuccessFlash(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, message);
    }
    
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    @GetMapping("/users/{id}/edit")
    public String editUserPage(@PathVariable Long id, Model model) {
        try {
            UserResponse user = userService.getUserById(id);
            UserUpdateRequest request = UserUpdateRequest.builder()
                    .name(user.getName())
                    .phoneNumber(user.getPhoneNumber())
                    .email(user.getEmail())
                    .address(user.getAddress())
                    .isActive(user.getIsActive())
                    .role(user.getRole())
                    .build();
            
            model.addAttribute("userUpdateRequest", request);
            model.addAttribute("userId", id);
            model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, "users");
            return AdminConstants.VIEW_EDIT_USER;
        } catch (ResourceNotFoundException e) {
            logger.error("User not found with id: {}", id, e);
            model.addAttribute(AdminConstants.ATTR_ERROR, getMessage(AdminConstants.MSG_USER_NOT_FOUND));
            return AdminConstants.REDIRECT_USERS;
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
            return AdminConstants.VIEW_EDIT_USER;
        }
        
        try {
            userService.updateUser(id, request);
            addSuccessFlash(redirectAttributes, getMessage(AdminConstants.MSG_USER_UPDATE_SUCCESS));
        } catch (EmailAlreadyExistsException e) {
            logger.warn("Email already exists: {}", request.getEmail());
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_EMAIL_ALREADY_EXISTS, request.getEmail()));
        } catch (ResourceNotFoundException e) {
            logger.error("User not found while updating id {}: {}", id, e.getMessage(), e);
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_USER_NOT_FOUND));
        } catch (Exception e) {
            logger.error("Error updating user id {}: {}", id, e.getMessage(), e);
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_USER_UPDATE_ERROR, e.getMessage()));
        }
        return AdminConstants.REDIRECT_USERS;
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            addSuccessFlash(redirectAttributes, getMessage(AdminConstants.MSG_USER_DELETE_SUCCESS));
        } catch (ResourceNotFoundException e) {
            logger.warn("User not found while deleting id {}: {}", id, e.getMessage());
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_USER_NOT_FOUND));
        } catch (Exception e) {
            logger.error("Error deleting user id {}: {}", id, e.getMessage(), e);
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_USER_DELETE_ERROR, e.getMessage()));
        }
        return AdminConstants.REDIRECT_USERS;
    }
}

