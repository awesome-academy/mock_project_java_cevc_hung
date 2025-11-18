package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.CategoryRevenueDTO;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.MonthlyRevenueDTO;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.RevenueResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.TopRatedTourDTO;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.BookingEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.RevenueEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.ReviewEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.RevenueRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.ReviewRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.projection.CategoryRevenueProjection;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.projection.MonthlyRevenueProjection;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.projection.TopRatedTourProjection;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional
public class RevenueService {
    
    private final RevenueRepository revenueRepository;
    private final ReviewRepository reviewRepository;
    private final MessageSource messageSource;

    public RevenueService(RevenueRepository revenueRepository,
                          ReviewRepository reviewRepository,
                          MessageSource messageSource) {
        this.revenueRepository = revenueRepository;
        this.reviewRepository = reviewRepository;
        this.messageSource = messageSource;
    }
    
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    public Page<RevenueResponse> getAllRevenues(String search, Pageable pageable) {
        Specification<RevenueEntity> spec = createSearchSpecification(search);
        Page<RevenueEntity> revenues = revenueRepository.findAll(spec, pageable);
        return revenues.map(this::convertToResponse);
    }

    public RevenueResponse getRevenueById(Long id) {
        RevenueEntity revenue = findRevenueById(id);
        return convertToResponse(revenue);
    }
    
    public Double getTotalRevenue() {
        Double sum = revenueRepository.sumTotalRevenue();
        return sum != null ? sum : 0.0;
    }

    public List<MonthlyRevenueDTO> getRevenueChartData(int monthsBack) {
        if (monthsBack <= 0) {
            return List.of();
        }

        LocalDate endMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate startMonth = endMonth.minusMonths(monthsBack - 1);

        List<MonthlyRevenueProjection> rawData = revenueRepository.findMonthlyRevenueFrom(startMonth);
        Map<String, Double> revenueMap = new LinkedHashMap<>();
        Map<String, Integer> bookingMap = new LinkedHashMap<>();
        for (MonthlyRevenueProjection row : rawData) {
            revenueMap.put(row.getPeriod(), row.getTotalRevenue());
            bookingMap.put(row.getPeriod(), row.getTotalBookings());
        }

        DateTimeFormatter keyFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Locale locale = LocaleContextHolder.getLocale();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM yyyy", locale);

        List<MonthlyRevenueDTO> chartData = new ArrayList<>();
        for (int i = 0; i < monthsBack; i++) {
            LocalDate month = startMonth.plusMonths(i);
            String key = month.format(keyFormatter);
            String label = month.format(labelFormatter);

            Double revenueValue = revenueMap.getOrDefault(key, 0.0);
            Integer bookingsValue = bookingMap.getOrDefault(key, 0);
            chartData.add(MonthlyRevenueDTO.builder()
                .period(key)
                .label(label)
                .totalRevenue(revenueValue)
                .totalBookings(bookingsValue)
                .build());
        }

        return chartData;
    }

    public List<CategoryRevenueDTO> getTopCategoriesByRevenue(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        List<CategoryRevenueProjection> rows = revenueRepository.findTopCategoriesByRevenue(limit);
        List<CategoryRevenueDTO> results = new ArrayList<>();
        for (CategoryRevenueProjection row : rows) {
            results.add(CategoryRevenueDTO.builder()
                .categoryId(row.getCategoryId())
                .label(row.getCategoryName())
                .totalRevenue(row.getTotalRevenue())
                .totalBookings(row.getTotalBookings())
                .build());
        }
        return results;
    }

    public List<TopRatedTourDTO> getTopRatedTours(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        List<TopRatedTourProjection> rows = reviewRepository.findTopRatedTours(
                ReviewEntity.Status.APPROVED.name(), limit);
        List<TopRatedTourDTO> results = new ArrayList<>();
        for (TopRatedTourProjection row : rows) {
            results.add(TopRatedTourDTO.builder()
                .tourId(row.getTourId())
                .label(row.getTourTitle())
                .averageRating(row.getAvgRating())
                .totalReviews(row.getTotalReviews())
                .build());
        }
        return results;
    }
    
    /**
     * Create a new revenue record for each payment (one-to-one relationship)
     * This allows full traceability of each payment
     * @param date The date of payment
     * @param bookingAmount The amount of the booking
     * @param booking The booking entity for reference
     * @return The created RevenueEntity
     */
    public RevenueEntity createRevenueForPayment(LocalDate date, Double bookingAmount, BookingEntity booking) {
        RevenueEntity revenue = RevenueEntity.builder()
                .date(date)
                .tourRevenue(bookingAmount)
                .totalRevenue(bookingAmount)
                .totalBookings(1)
                .booking(booking)
                .build();
        
        return revenueRepository.save(revenue);
    }
    
    private Specification<RevenueEntity> createSearchSpecification(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }
            List<Predicate> predicates = new ArrayList<>();
            
            try {
                LocalDate searchDate = LocalDate.parse(search);
                predicates.add(cb.equal(root.get("date"), searchDate));
            } catch (Exception e) {
                //
            }
            
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private RevenueResponse convertToResponse(RevenueEntity revenue) {
        return RevenueResponse.builder()
                .id(revenue.getId())
                .date(revenue.getDate())
                .totalRevenue(revenue.getTotalRevenue())
                .tourRevenue(revenue.getTourRevenue())
                .totalBookings(revenue.getTotalBookings())
                .bookingId(revenue.getBooking() != null ? revenue.getBooking().getId() : null)
                .createdAt(revenue.getCreatedAt())
                .updatedAt(revenue.getUpdatedAt())
                .build();
    }

    private RevenueEntity findRevenueById(Long id) {
        return revenueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("revenue.error.not_found", id)));
    }
}

