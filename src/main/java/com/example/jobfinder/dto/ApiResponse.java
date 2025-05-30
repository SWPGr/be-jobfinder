package com.example.jobfinder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) //Khi đưa object sang json thì thằng nào null sẽ không cho vào json
public class ApiResponse <T>{
    int code = 1000; //success
    String message;
    T result;
}
