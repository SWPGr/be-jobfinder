// dto/response/SimpleNameResponse.java
package com.example.jobfinder.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimpleNameResponse {
    Long id;
    String name;
}