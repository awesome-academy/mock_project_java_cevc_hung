package com.mock_project_java_cevc_hung.hunglpmockjava.repository;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.RevenueEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.projection.CategoryRevenueProjection;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.projection.MonthlyRevenueProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RevenueRepository extends JpaRepository<RevenueEntity, Long>, JpaSpecificationExecutor<RevenueEntity> {
    
    Optional<RevenueEntity> findByDate(LocalDate date);

    @Query("select coalesce(sum(r.totalRevenue), 0) from RevenueEntity r")
    Double sumTotalRevenue();

    @Query(
            value = """
                    SELECT DATE_FORMAT(r.date, '%Y-%m') AS period,
                           COALESCE(SUM(r.total_revenue), 0) AS totalRevenue,
                           COALESCE(SUM(r.total_bookings), 0) AS totalBookings
                    FROM revenues r
                    WHERE r.date >= :startDate
                    GROUP BY DATE_FORMAT(r.date, '%Y-%m')
                    ORDER BY period
                    """,
            nativeQuery = true
    )
    List<MonthlyRevenueProjection> findMonthlyRevenueFrom(@Param("startDate") LocalDate startDate);

    @Query(
            value = """
                    SELECT c.id AS categoryId,
                           COALESCE(c.name, 'Uncategorized') AS categoryName,
                           COALESCE(SUM(r.total_revenue), 0) AS totalRevenue,
                           COALESCE(SUM(r.total_bookings), 0) AS totalBookings
                    FROM revenues r
                    LEFT JOIN bookings b ON r.booking_id = b.id
                    LEFT JOIN tours t ON b.tour_id = t.id
                    LEFT JOIN categories c ON t.category_id = c.id
                    GROUP BY c.id, c.name
                    ORDER BY totalRevenue DESC
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<CategoryRevenueProjection> findTopCategoriesByRevenue(@Param("limit") int limit);
}
