package com.example.jobfinder.dto.employer;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployerSearchRequest {
    private String name;
    private String location;
    private Long organizationId;
    private String keyword;
    private Integer page = 1;
    private Integer size = 10;
}
