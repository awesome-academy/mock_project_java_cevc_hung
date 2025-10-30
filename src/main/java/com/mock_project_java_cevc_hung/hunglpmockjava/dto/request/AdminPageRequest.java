package com.mock_project_java_cevc_hung.hunglpmockjava.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPageRequest {
    
    private Integer page;
    
    private Integer size;
    
    private String sortBy;
    
    private String sortDir;
    
    private String search;
    
    private String status;
    
    public int getPage() {
        return page != null ? page : 0;
    }
    
    public int getSize() {
        return size != null ? size : 10;
    }
    
    public String getSortBy() {
        return sortBy != null && !sortBy.isEmpty() ? sortBy : "id";
    }
    
    public String getSortDir() {
        return sortDir != null && !sortDir.isEmpty() ? sortDir : "desc";
    }
}

