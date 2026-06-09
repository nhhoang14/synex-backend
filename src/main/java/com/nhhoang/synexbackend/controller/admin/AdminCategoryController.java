package com.nhhoang.synexbackend.controller.admin;

import com.nhhoang.synexbackend.dto.request.CategoryRequest;
import com.nhhoang.synexbackend.entity.Category;
import com.nhhoang.synexbackend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin("*")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Category> create(@ModelAttribute CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        // Logic: Upload request.getImageFile() lên Cloudinary/S3 và setImageUrl
        // category.setImageUrl(fileService.upload(request.getImageFile()));
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Category> update(@PathVariable Long id, @ModelAttribute CategoryRequest request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        existing.setName(request.getName());
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
             // Logic: Upload ảnh mới và cập nhật URL
             // existing.setImageUrl(fileService.upload(request.getImageFile()));
        }
        return ResponseEntity.ok(categoryRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}