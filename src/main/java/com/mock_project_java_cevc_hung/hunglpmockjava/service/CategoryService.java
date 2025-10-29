package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.CategoryCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.CategoryUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.CategoryResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.CategoryEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.BusinessException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.CategoryRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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

    public Page<CategoryResponse> getAllCategories(String search, Pageable pageable) {
        Specification<CategoryEntity> spec = createSearchSpecification(search);
        Page<CategoryEntity> categories = categoryRepository.findAll(spec, pageable);
        return categories.map(this::convertToResponse);
    }

    private Specification<CategoryEntity> createSearchSpecification(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }
            
            String searchPattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(cb.like(cb.lower(root.get("name")), searchPattern));
            predicates.add(cb.like(cb.lower(root.get("slug")), searchPattern));
            
            return cb.or(predicates.toArray(new Predicate[0]));
        };
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
                .map(this::convertToSimpleResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        CategoryEntity category = findCategoryById(id);
        return convertToResponse(category);
    }

    public CategoryResponse createCategory(CategoryCreateRequest request) {
        CategoryEntity category = CategoryEntity.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .build();

        if (request.getParentId() != null) {
            CategoryEntity parent = findParentCategoryById(request.getParentId());
            category.setParent(parent);
        }

        CategoryEntity savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }

    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        CategoryEntity category = findCategoryById(id);

        category.setName(request.getName());
        category.setSlug(request.getSlug());

        if (request.getParentId() != null) {
            CategoryEntity parent = findParentCategoryById(request.getParentId());
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        CategoryEntity savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }

    public void deleteCategory(Long id) {
        CategoryEntity category = findCategoryById(id);
        
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new BusinessException("Cannot delete category with subcategories. Please delete subcategories first.");
        }
        
        if (category.getTours() != null && !category.getTours().isEmpty()) {
            throw new BusinessException("Cannot delete category with tours. Please move or delete tours first.");
        }
        
        categoryRepository.delete(category);
    }

    private CategoryResponse convertToResponse(CategoryEntity category) {
        return convertToResponse(category, Integer.MAX_VALUE);
    }

    private CategoryResponse convertToResponse(CategoryEntity category, int maxDepth) {
        CategoryResponse.CategoryResponseBuilder builder = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .tourCount(category.getTours() != null ? (long) category.getTours().size() : 0L)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt());

        if (maxDepth > 0 && category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<CategoryResponse> children = category.getChildren().stream()
                    .map(child -> convertToResponse(child, maxDepth - 1))
                    .collect(Collectors.toList());
            builder.children(children);
        } else {
            builder.children(null);
        }

        return builder.build();
    }

    private CategoryResponse convertToSimpleResponse(CategoryEntity category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .children(null)
                .tourCount(category.getTours() != null ? (long) category.getTours().size() : 0L)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private CategoryEntity findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private CategoryEntity findParentCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + id));
    }
}
