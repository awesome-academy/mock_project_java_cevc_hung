package com.mock_project_java_cevc_hung.hunglpmockjava.entity;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "comments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommentEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "review_id")
    private ReviewEntity review;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Lob
    @NotBlank
    @Column(nullable = false)
    private String content;
}
