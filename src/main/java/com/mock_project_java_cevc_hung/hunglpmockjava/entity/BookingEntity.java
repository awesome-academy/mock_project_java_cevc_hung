package com.mock_project_java_cevc_hung.hunglpmockjava.entity;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private TourEntity tour;

    private Integer qty;
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PENDING;

    private String paymentRef;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;

    public enum Status {
        PENDING, PAID, CANCELLED, REFUNDED
    }
}

