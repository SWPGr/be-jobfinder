package com.example.jobfinder.controller;

import com.example.jobfinder.dto.SimpleNameCreationRequest;
import com.example.jobfinder.dto.SimpleNameUpdateRequest;
import com.example.jobfinder.dto.SimpleNameResponse;
import com.example.jobfinder.service.BaseNameService; // Vẫn dùng BaseNameService (abstract class)
import com.example.jobfinder.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Lớp này là abstract, sẽ không được Spring tạo bean trực tiếp
@Slf4j
public abstract class BaseNameController {

    // Phương thức trừu tượng để lấy service cụ thể
    protected abstract BaseNameService getService();
    // Phương thức trừu tượng để lấy base path cụ thể (ví dụ: "/categories", "/job-levels")
    protected abstract String getBasePath();

    @PostMapping
    public ApiResponse<SimpleNameResponse> create(@RequestBody @Valid SimpleNameCreationRequest request) {
        log.info("Received request to create {} (via generic controller): {}", getBasePath(), request.getName());
        SimpleNameResponse response = getService().create(request);
        return ApiResponse.<SimpleNameResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message(getBasePath() + " created successfully")
                .result(response)
                .build();
    }

    @GetMapping
    public ApiResponse<List<SimpleNameResponse>> getAll() {
        log.info("Received request to get all {}s (via generic controller).", getBasePath());
        List<SimpleNameResponse> responses = getService().getAll();
        return ApiResponse.<List<SimpleNameResponse>>builder()
                .code(HttpStatus.OK.value())
                .message(getBasePath() + "s retrieved successfully")
                .result(responses)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SimpleNameResponse> getById(@PathVariable Long id) {
        log.info("Received request to get {} with ID {} (via generic controller).", getBasePath(), id);
        SimpleNameResponse response = getService().getById(id);
        return ApiResponse.<SimpleNameResponse>builder()
                .code(HttpStatus.OK.value())
                .message(getBasePath() + " retrieved successfully")
                .result(response)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<SimpleNameResponse> update(@PathVariable Long id, @RequestBody @Valid SimpleNameUpdateRequest request) {
        log.info("Received request to update {} ID {} (via generic controller): {}", getBasePath(), id, request.getName());
        SimpleNameResponse updatedResponse = getService().update(id, request);
        return ApiResponse.<SimpleNameResponse>builder()
                .code(HttpStatus.OK.value())
                .message(getBasePath() + " updated successfully")
                .result(updatedResponse)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.info("Received request to delete {} with ID {} (via generic controller).", getBasePath(), id);
        getService().delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message(getBasePath() + " deleted successfully")
                .build();
    }
}