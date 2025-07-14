// dto/response/SimpleNameResponse.java
package com.example.jobfinder.dto.simple;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimpleNameResponse {
    Long id;
    String name;
}