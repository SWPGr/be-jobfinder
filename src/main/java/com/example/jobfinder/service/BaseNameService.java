// service/BaseNameService.java (Interface chung)
package com.example.jobfinder.service;

import com.example.jobfinder.dto.simple.SimpleNameCreationRequest;
import com.example.jobfinder.dto.simple.SimpleNameUpdateRequest;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.repository.BaseNameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseNameService<T> {

    protected final BaseNameRepository<T, Long> repository;

    protected abstract T createEntity(SimpleNameCreationRequest request);
    protected abstract void updateEntity(T entity, SimpleNameUpdateRequest request);
    protected abstract SimpleNameResponse toResponse(T entity);
    protected abstract ErrorCode getExistedErrorCode();
    protected abstract ErrorCode getNotFoundErrorCode();
    protected abstract String getEntityNameForLog(); // Ví dụ: "Category", "Job Level"

    // CREATE
    @Transactional
    public SimpleNameResponse create(SimpleNameCreationRequest request) {
        log.info("Attempting to create {}: {}", getEntityNameForLog(), request.getName());

        if (repository.existsByName(request.getName())) {
            log.warn("{} with name '{}' already exists.", getEntityNameForLog(), request.getName());
            throw new AppException(getExistedErrorCode());
        }

        T entity = createEntity(request);
        T savedEntity = repository.save(entity);
        log.info("{} created successfully with ID: {}", getEntityNameForLog(), repository.getId(savedEntity));
        return toResponse(savedEntity);
    }

    // READ All
    public List<SimpleNameResponse> getAll() {
        log.info("Fetching all {}s.", getEntityNameForLog());
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // READ By ID
    public SimpleNameResponse getById(Long id) {
        log.info("Fetching {} with ID: {}", getEntityNameForLog(), id);
        T entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("{} with ID {} not found.", getEntityNameForLog(), id);
                    return new AppException(getNotFoundErrorCode());
                });
        log.info("{} found: {}", getEntityNameForLog(), repository.getId(entity)); // Log ID thay vì toString()
        return toResponse(entity);
    }

    // UPDATE
    @Transactional
    public SimpleNameResponse update(Long id, SimpleNameUpdateRequest request) {
        log.info("Attempting to update {} with ID: {}", getEntityNameForLog(), id);
        T existingEntity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("{} with ID {} not found for update.", getEntityNameForLog(), id);
                    return new AppException(getNotFoundErrorCode());
                });

        Optional<T> entityWithNewName = repository.findByName(request.getName());
        if (entityWithNewName.isPresent() && !repository.getId(entityWithNewName.get()).equals(id)) {
            log.warn("Update failed: {} with name '{}' already exists for another ID.", getEntityNameForLog(), request.getName());
            throw new AppException(getExistedErrorCode());
        }

        updateEntity(existingEntity, request);
        T updatedEntity = repository.save(existingEntity);
        log.info("{} with ID {} updated successfully.", getEntityNameForLog(), repository.getId(updatedEntity));
        return toResponse(updatedEntity);
    }

    // DELETE
    @Transactional
    public void delete(Long id) {
        log.info("Attempting to delete {} with ID: {}", getEntityNameForLog(), id);
        if (!repository.existsById(id)) {
            log.warn("{} with ID {} not found for deletion.", getEntityNameForLog(), id);
            throw new AppException(getNotFoundErrorCode());
        }
        repository.deleteById(id);
        log.info("{} with ID {} deleted successfully.", getEntityNameForLog(), id);
    }
}