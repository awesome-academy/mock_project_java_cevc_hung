package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.CategoryResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.CategoryEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryEntity testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new CategoryEntity();
        testCategory.setName("Adventure");
        testCategory.setSlug("adventure");
    }

    @Test
    @DisplayName("Should get all categories with pagination")
    void getAllCategories_WithPageable_ShouldReturnPagedCategories() {
        // Given
        Page<CategoryEntity> page = new PageImpl<>(List.of(testCategory));
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<CategoryResponse> result = categoryService.getAllCategories(pageable);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Adventure");
        verify(categoryRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get all categories as list")
    void getAllCategoriesAsList_ShouldReturnAllCategories() {
        // Given
        CategoryEntity category2 = new CategoryEntity();
        category2.setName("Beach");
        category2.setSlug("beach");
        when(categoryRepository.findAll()).thenReturn(List.of(testCategory, category2));

        // When
        List<CategoryResponse> result = categoryService.getAllCategoriesAsList();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Adventure");
        assertThat(result.get(1).getName()).isEqualTo("Beach");
    }

    @Test
    @DisplayName("Should get parent categories only")
    void getParentCategories_ShouldReturnOnlyParentCategories() {
        // Given
        when(categoryRepository.findByParentIsNull()).thenReturn(List.of(testCategory));

        // When
        List<CategoryResponse> result = categoryService.getParentCategories();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(categoryRepository, times(1)).findByParentIsNull();
    }

    @Test
    @DisplayName("Should get category by id")
    void getCategoryById_ExistingCategory_ShouldReturnCategory() {
        // Given
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));

        // When
        CategoryResponse result = categoryService.getCategoryById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Adventure");
        assertThat(result.getSlug()).isEqualTo("adventure");
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void getCategoryById_NonExistingCategory_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    @DisplayName("Should return empty list when no categories exist")
    void getAllCategoriesAsList_NoCategories_ShouldReturnEmptyList() {
        // Given
        when(categoryRepository.findAll()).thenReturn(List.of());

        // When
        List<CategoryResponse> result = categoryService.getAllCategoriesAsList();

        // Then
        assertThat(result).isEmpty();
    }
}
