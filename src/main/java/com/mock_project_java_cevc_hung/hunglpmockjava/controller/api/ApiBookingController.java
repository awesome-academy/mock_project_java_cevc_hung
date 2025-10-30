package com.mock_project_java_cevc_hung.hunglpmockjava.controller.api;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.BookingCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiBookingResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiPaginatedResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.BusinessException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.security.UserDetailsImpl;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ApiBookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(
            @Valid @RequestBody BookingCreateRequest request,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                error.put("message", "You must be logged in to create a booking");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();

            ApiBookingResponse response = bookingService.createBooking(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (ResourceNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Resource not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (BusinessException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Business logic error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/bookings/my")
    public ResponseEntity<?> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                error.put("message", "You must be logged in to view your bookings");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();

            if (page < 0) page = 0;
            if (size < 1) size = 10;
            if (size > 100) size = 100;

            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                       Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ApiBookingResponse> bookings = bookingService.getUserBookings(userId, pageable);

            ApiPaginatedResponse<ApiBookingResponse> response = ApiPaginatedResponse.<ApiBookingResponse>builder()
                    .content(bookings.getContent())
                    .page(bookings.getNumber())
                    .size(bookings.getSize())
                    .totalElements(bookings.getTotalElements())
                    .totalPages(bookings.getTotalPages())
                    .first(bookings.isFirst())
                    .last(bookings.isLast())
                    .hasNext(bookings.hasNext())
                    .hasPrevious(bookings.hasPrevious())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                error.put("message", "You must be logged in to cancel a booking");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();

            ApiBookingResponse response = bookingService.cancelUserBooking(id, userId);
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Resource not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (BusinessException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Business logic error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

