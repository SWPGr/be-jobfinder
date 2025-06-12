// service/CategoryService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.simple.SimpleNameCreationRequest;
import com.example.jobfinder.dto.simple.SimpleNameUpdateRequest;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.CategoryMapper;
import com.example.jobfinder.model.Category;
import com.example.jobfinder.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service // Annotation này để Spring tạo bean cho CategoryService
public class CategoryService extends BaseNameService<Category> {

    private final CategoryMapper categoryMapper;

    // Constructor để Spring tự động inject CategoryRepository và CategoryMapper
    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        super(categoryRepository); // Gọi constructor của lớp cha
        this.categoryMapper = categoryMapper;
    }

    @Override
    protected Category createEntity(SimpleNameCreationRequest request) {
        return categoryMapper.toCategory(request);
    }

    @Override
    protected void updateEntity(Category entity, SimpleNameUpdateRequest request) {
        categoryMapper.updateCategory(entity, request);
    }

    @Override
    protected SimpleNameResponse toResponse(Category entity) {
        return categoryMapper.toCategoryResponse(entity);
    }

    @Override
    protected ErrorCode getExistedErrorCode() {
        return ErrorCode.CATEGORY_NAME_EXISTED;
    }

    @Override
    protected ErrorCode getNotFoundErrorCode() {
        return ErrorCode.CATEGORY_NOT_FOUND;
    }

    @Override
    protected String getEntityNameForLog() {
        return "Category";
    }
}