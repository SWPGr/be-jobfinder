package com.example.jobfinder.model.enums;

import lombok.Getter;

@Getter
public enum ApplicationStatus {
    PENDING("PENDING"),
    REVIEWED("REVIEWED"),
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED");

    private final String value;

    ApplicationStatus(String value) {
        this.value = value;
    }

    //Nhận giá trị là String và trả về giá trị là Enum trong bảng, không phần biệt hoa thường
    public static ApplicationStatus fromString(String text) {
        for (ApplicationStatus b : ApplicationStatus.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No status with text " + text + " found");
    }
}