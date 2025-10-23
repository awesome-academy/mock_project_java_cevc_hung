package com.mock_project_java_cevc_hung.hunglpmockjava.entity;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryEntity extends BaseEntity {
    @NotBlank
    @Column(nullable = false)
    private String name;

    private String slug;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private CategoryEntity parent;

    // Relationships
    @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
    private List<CategoryEntity> children;

    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST)
    private List<TourEntity> tours;
}

