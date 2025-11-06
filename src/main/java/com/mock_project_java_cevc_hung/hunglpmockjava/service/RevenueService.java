package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.RevenueResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.BookingEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.RevenueEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.RevenueRepository;
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
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RevenueService {
    
    private final RevenueRepository revenueRepository;
    
    private final MessageSource messageSource;

    public RevenueService(RevenueRepository revenueRepository, MessageSource messageSource) {
        this.revenueRepository = revenueRepository;
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

