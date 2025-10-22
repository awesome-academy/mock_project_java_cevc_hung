package com.mock_project_java_cevc_hung.hunglpmockjava.entity;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "revenues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueEntity extends BaseEntity {
    private LocalDate date;
    private Double totalRevenue;
    private Double tourRevenue;
    private Double commissionRevenue;
    private Integer totalBookings;
}
