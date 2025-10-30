package com.mock_project_java_cevc_hung.hunglpmockjava.controller.admin;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.CategoryCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.AdminPageRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.CategoryUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.CategoryResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.BusinessException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.CategoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    // Constants
    private static final String BASE_PATH = "/admin/categories";
    private static final String VIEW_BASE = "admin/categories/";
    private static final String VIEW_INDEX = VIEW_BASE + "index";
    private static final String VIEW_CREATE = VIEW_BASE + "create";
    private static final String VIEW_EDIT = VIEW_BASE + "edit";
    private static final String REDIRECT_CATEGORIES = "redirect:" + BASE_PATH;
    private static final String REDIRECT_CREATE = REDIRECT_CATEGORIES + "/create";

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public String categoriesPage(
        @ModelAttribute AdminPageRequest request,
        Model model
    ) {
        
        Sort sort = request.getSortDir().equalsIgnoreCase("desc") ? 
                   Sort.by(request.getSortBy()).descending() : Sort.by(request.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        Page<CategoryResponse> categories = categoryService.getAllCategories(request.getSearch(), pageable);
        List<CategoryResponse> parentCategories = categoryService.getParentCategories();
        
        model.addAttribute("categories", categories);
        model.addAttribute("parentCategories", parentCategories);
        model.addAttribute("currentPage", request.getPage());
        model.addAttribute("totalPages", categories.getTotalPages());
        model.addAttribute("totalItems", categories.getTotalElements());
        model.addAttribute("sortBy", request.getSortBy());
        model.addAttribute("sortDir", request.getSortDir());
        model.addAttribute("search", request.getSearch());
        model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_CATEGORIES);
        
        return VIEW_INDEX;
    }

    @GetMapping("/categories/create")
    public String createCategoryPage(Model model) {
        List<CategoryResponse> parentCategories = categoryService.getParentCategories();
        model.addAttribute("categoryCreateRequest", new CategoryCreateRequest());
        model.addAttribute("parentCategories", parentCategories);
        model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_CATEGORIES);
        return VIEW_CREATE;
    }

    @PostMapping("/categories/create")
    public String createCategory(
            @Valid @ModelAttribute CategoryCreateRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            List<CategoryResponse> parentCategories = categoryService.getParentCategories();
            model.addAttribute("parentCategories", parentCategories);
            model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_CATEGORIES);
            return VIEW_CREATE;
        }
        
        try {
            categoryService.createCategory(request);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, "Category created successfully!");
            return REDIRECT_CATEGORIES;
        } catch (BusinessException e) {
            logger.warn("Business exception while creating category: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, e.getMessage());
            return REDIRECT_CREATE;
        } catch (ResourceNotFoundException e) {
            logger.warn("Resource not found while creating category: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, e.getMessage());
            return REDIRECT_CREATE;
        } catch (Exception e) {
            logger.error("Unexpected error while creating category: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error creating category: " + e.getMessage());
            return REDIRECT_CREATE;
        }
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategoryPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            CategoryResponse category = categoryService.getCategoryById(id);
            List<CategoryResponse> parentCategories = categoryService.getParentCategories();
            
            CategoryUpdateRequest updateRequest = CategoryUpdateRequest.builder()
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParentId())
                .build();
            
            model.addAttribute("categoryUpdateRequest", updateRequest);
            model.addAttribute("category", category);
            model.addAttribute("parentCategories", parentCategories);
            model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_CATEGORIES);
            return VIEW_EDIT;
        } catch (ResourceNotFoundException e) {
            logger.warn("Category not found with id: {}", id);
            return "redirect:" + REDIRECT_CATEGORIES;
        } catch (Exception e) {
            logger.error("Error loading edit category page for id {}: {}", id, e.getMessage(), e);
            return "redirect:" + REDIRECT_CATEGORIES;
        }
    }

    @PostMapping("/categories/{id}/edit")
    public String updateCategory(@PathVariable Long id,
                               @Valid @ModelAttribute CategoryUpdateRequest request,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        
        if (result.hasErrors()) {
            try {
                CategoryResponse category = categoryService.getCategoryById(id);
                List<CategoryResponse> parentCategories = categoryService.getParentCategories();
                model.addAttribute("category", category);
                model.addAttribute("parentCategories", parentCategories);
                model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_CATEGORIES);
                return VIEW_EDIT;
            } catch (ResourceNotFoundException e) {
                logger.warn("Category not found with id: {}", id);
                redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, e.getMessage());
                return "redirect:" + REDIRECT_CATEGORIES;
            } catch (Exception e) {
                logger.error("Error loading edit category page for id {}: {}", id, e.getMessage(), e);
                redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error loading category: " + e.getMessage());
                return "redirect:" + REDIRECT_CATEGORIES;
            }
        }
        
        try {
            categoryService.updateCategory(id, request);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, "Category updated successfully!");
            return REDIRECT_CATEGORIES;
        } catch (ResourceNotFoundException e) {
            logger.warn("Category not found while updating id {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, e.getMessage());
            return REDIRECT_CATEGORIES;
        } catch (BusinessException e) {
            logger.warn("Business exception while updating category id {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, e.getMessage());
            return REDIRECT_CATEGORIES + "/" + id + "/edit";
        } catch (Exception e) {
            logger.error("Unexpected error while updating category id {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error updating category: " + e.getMessage());
            return REDIRECT_CATEGORIES + "/" + id + "/edit";
        }
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, "Category deleted successfully!");
        } catch (ResourceNotFoundException e) {
            logger.warn("Category not found while deleting id {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, e.getMessage());
        } catch (BusinessException e) {
            logger.warn("Business exception while deleting category id {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while deleting category id {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error deleting category: " + e.getMessage());
        }
        return REDIRECT_CATEGORIES;
    }
}
