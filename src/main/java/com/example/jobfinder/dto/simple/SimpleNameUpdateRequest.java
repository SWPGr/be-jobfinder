// dto/request/SimpleNameUpdateRequest.java (thường giống Creation cho trường hợp này)
package com.example.jobfinder.dto.simple;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimpleNameUpdateRequest {
    @NotBlank(message = "NAME_BLANK")
    @Size(min = 3, max = 100, message = "NAME_INVALID")
    String name;
}