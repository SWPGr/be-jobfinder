
package com.example.jobfinder.dto.application;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateFilterRequest {
    String fullName;
    String email;
    String location;
    String experienceName;
    String educationName;
    Boolean isPremium;
}