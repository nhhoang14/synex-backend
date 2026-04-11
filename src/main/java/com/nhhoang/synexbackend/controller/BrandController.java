package com.nhhoang.synexbackend.controller;

import com.nhhoang.synexbackend.model.Brand;
import com.nhhoang.synexbackend.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@CrossOrigin("*")
public class BrandController {

    private final BrandRepository brandRepository;

    @GetMapping
    public List<Brand> getAll() {
        return brandRepository.findAll();
    }
}