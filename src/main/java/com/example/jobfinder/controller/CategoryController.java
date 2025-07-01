// controller/CategoryController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.service.BaseNameService;
import com.example.jobfinder.service.CategoryService; // Import CategoryService
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories") // Đặt base path cho mỗi controller cụ thể
public class CategoryController extends BaseNameController {

    private final CategoryService categoryService; // Inject service cụ thể

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    protected BaseNameService getService() {
        return categoryService; // Trả về instance của service cụ thể
    }

    @Override
    protected String getBasePath() {
        return "Category";
    }
}