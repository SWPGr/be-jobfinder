package com.example.jobfinder.dto.job;

import com.example.jobfinder.model.Category;

public record CategoryDTO(Long id, String name) {
    public static CategoryDTO fromEntity(Category category) {
        return new CategoryDTO(category.getId(), category.getName());
    }
}