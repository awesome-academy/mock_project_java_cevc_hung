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
public class TopRatedTourDTO {
    private Long tourId;
    private String label;
    private Double averageRating;
    private Long totalReviews;
}

