package com.example.jobfinder.dto.job_seeker;

import com.example.jobfinder.dto.user.UserResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobSeekerSearchResponse {
    private List<UserResponse> data;
    private Long totalHits;
    private int page;
    private int size;

    public boolean isHasNext() {
        return (long) page * size < totalHits;
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

}
