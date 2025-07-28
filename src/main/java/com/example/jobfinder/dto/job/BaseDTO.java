package com.example.jobfinder.dto.job;

public interface BaseDTO<E, D> {
    D fromEntity(E entity);
}