package com.example.jobfinder.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrganizationType {
    STARTUP("STARTUP"),
    ENTERPRISE("ENTERPRISE"),
    NON_PROFIT("NON_PROFIT"),
    GOVERNMENT("GOVERNMENT"),
    FREELANCE("FREELANCE"),
    OTHER("OTHER");

    private final String value;

    OrganizationType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static OrganizationType fromString(String text) {
        for (OrganizationType b : OrganizationType.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No status with text " + text + " found");
    }

    @Override
    public String toString() {
        return this.value;
    }
}
