package com.example.jobfinder.dto.employer;

import com.example.jobfinder.dto.user.UserResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployerSearchResponse {
    private List<UserResponse> data;
    private long totalHits;
    private int page;
    private int size;
    
    public boolean isHasNext() {
        return (long) page * size < totalHits;
    }
    
    public boolean isHasPrevious() {
        return page > 1;
    }
}
