
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
    // Các trường để lọc
    String fullName;
    String email;
    String location;
    String experienceName; // Tên kinh nghiệm (ví dụ: "Entry Level", "Mid Level")
    String educationName;  // Tên học vấn (ví dụ: "Bachelor's Degree", "Master's Degree")
    Boolean isPremium; // Nếu bạn muốn lọc ứng viên premium (từ User Entity)
}