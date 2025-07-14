package com.example.jobfinder.dto.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobSearchResponse {
    private List<JobResponse> data;
    private long totalHits;
    private int page;
    private int size;
}
