package com.mock_project_java_cevc_hung.hunglpmockjava.entity;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tours")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TourEntity extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String location;
    private String thumbnailUrl;

    @Column(nullable = false)
    private Integer seatsTotal;

    @Column(nullable = false)
    @Builder.Default
    private Integer seatsAvailable = 1;

    @Builder.Default
    private Double ratingAvg = 0.0;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    // Relationships
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingEntity> bookings;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewEntity> reviews;

    public enum Status { ACTIVE, INACTIVE }
}
