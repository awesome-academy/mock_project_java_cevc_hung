package com.mock_project_java_cevc_hung.hunglpmockjava.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRevenueDTO {
    private Long categoryId;
    private String label;
    private Double totalRevenue;
    private Integer totalBookings;
}

