package com.mock_project_java_cevc_hung.hunglpmockjava.controller.admin;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.CategoryCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.CategoryUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.CategoryResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.CategoryService;
import jakarta.validation.Valid;
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

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/categories")
    public String categoriesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CategoryResponse> categories = categoryService.getAllCategories(pageable);
        List<CategoryResponse> parentCategories = categoryService.getParentCategories();
        
        model.addAttribute("categories", categories);
        model.addAttribute("parentCategories", parentCategories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categories.getTotalPages());
        model.addAttribute("totalItems", categories.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("activePage", "categories");
        
        return "admin/categories/index";
    }

    @GetMapping("/categories/create")
    public String createCategoryPage(Model model) {
        List<CategoryResponse> parentCategories = categoryService.getParentCategories();
        model.addAttribute("categoryCreateRequest", new CategoryCreateRequest());
        model.addAttribute("parentCategories", parentCategories);
        model.addAttribute("activePage", "categories");
        return "admin/categories/create";
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
            model.addAttribute("activePage", "categories");
            return "admin/categories/create";
        }
        
        try {
            categoryService.createCategory(request);
            redirectAttributes.addFlashAttribute("success", "Category created successfully!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating category: " + e.getMessage());
            return "redirect:/admin/categories/create";
        }
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategoryPage(@PathVariable Long id, Model model) {
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
            model.addAttribute("activePage", "categories");
            return "admin/categories/edit";
        } catch (Exception e) {
            return "redirect:/admin/categories?error=" + e.getMessage();
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
                model.addAttribute("activePage", "categories");
                return "admin/categories/edit";
            } catch (Exception e) {
                return "redirect:/admin/categories?error=" + e.getMessage();
            }
        }
        
        try {
            categoryService.updateCategory(id, request);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating category: " + e.getMessage());
            return "redirect:/admin/categories/" + id + "/edit";
        }
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting category: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
