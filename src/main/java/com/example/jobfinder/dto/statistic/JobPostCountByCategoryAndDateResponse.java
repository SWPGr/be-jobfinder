// src/main/java/com/example/jobfinder/dto/response/JobPostCountByCategoryAndDateResponse.java
package com.example.jobfinder.dto.statistic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostCountByCategoryAndDateResponse {
    private String categoryName;
    private long jobCount;
}