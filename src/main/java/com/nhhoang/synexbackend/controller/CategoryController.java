package com.nhhoang.synexbackend.controller;

import com.nhhoang.synexbackend.model.Category;
import com.nhhoang.synexbackend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> getAll(){
        return categoryRepository.findAll();
    }
}