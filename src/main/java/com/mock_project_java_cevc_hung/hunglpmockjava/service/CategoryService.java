package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.CategoryCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.CategoryUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.CategoryResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.CategoryEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        Page<CategoryEntity> categories = categoryRepository.findAll(pageable);
        return categories.map(this::convertToResponse);
    }

    public List<CategoryResponse> getAllCategoriesAsList() {
        List<CategoryEntity> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getParentCategories() {
        List<CategoryEntity> categories = categoryRepository.findByParentIsNull();
        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return convertToResponse(category);
    }

    public CategoryResponse createCategory(CategoryCreateRequest request) {
        CategoryEntity category = CategoryEntity.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .build();

        if (request.getParentId() != null) {
            CategoryEntity parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found with id: " + request.getParentId()));
            category.setParent(parent);
        }

        CategoryEntity savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }

    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        category.setName(request.getName());
        category.setSlug(request.getSlug());

        if (request.getParentId() != null) {
            CategoryEntity parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found with id: " + request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        CategoryEntity savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }

    public void deleteCategory(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new RuntimeException("Cannot delete category with subcategories. Please delete subcategories first.");
        }
        
        if (category.getTours() != null && !category.getTours().isEmpty()) {
            throw new RuntimeException("Cannot delete category with tours. Please move or delete tours first.");
        }
        
        categoryRepository.delete(category);
    }

    private CategoryResponse convertToResponse(CategoryEntity category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .children(category.getChildren() != null ? 
                    category.getChildren().stream()
                            .map(this::convertToResponse)
                            .collect(Collectors.toList()) : null)
                .tourCount(category.getTours() != null ? (long) category.getTours().size() : 0L)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}