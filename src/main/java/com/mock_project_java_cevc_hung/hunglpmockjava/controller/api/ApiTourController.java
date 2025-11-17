package com.mock_project_java_cevc_hung.hunglpmockjava.controller.api;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiPaginatedResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiTourResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ApiTourController {

    @Autowired
    private TourService tourService;

    @GetMapping("/tours")
    public ResponseEntity<ApiPaginatedResponse<ApiTourResponse>> getTours(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword
    ) {
        
        try {
            if (page < 0) page = 0;
            if (size < 1) size = 10;
            if (size > 100) size = 100;
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                       Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ApiTourResponse> tours = tourService.getPublicTours(categoryId, keyword, pageable);
            
            ApiPaginatedResponse<ApiTourResponse> response = ApiPaginatedResponse.<ApiTourResponse>builder()
                    .content(tours.getContent())
                    .page(tours.getNumber())
                    .size(tours.getSize())
                    .totalElements(tours.getTotalElements())
                    .totalPages(tours.getTotalPages())
                    .first(tours.isFirst())
                    .last(tours.isLast())
                    .hasNext(tours.hasNext())
                    .hasPrevious(tours.hasPrevious())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tours/{id}")
    public ResponseEntity<ApiTourResponse> getTourById(@PathVariable Long id) {
        try {
            ApiTourResponse tour = tourService.getPublicTourById(id);
            return ResponseEntity.ok(tour);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
