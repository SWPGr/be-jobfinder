package com.example.jobfinder.dto.statistic_admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyApplicationCountResponse {
    private LocalDate date;
    private Long count;
}