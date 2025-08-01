package com.example.jobfinder.dto.application;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationStatusUpdateRequest {
    String status;
    String employerMessage;
}