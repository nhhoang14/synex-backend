package com.nhhoang.synexbackend.controller.client;

import com.nhhoang.synexbackend.entity.Product;
import com.nhhoang.synexbackend.service.ProductService;
// import com.nhhoang.synexbackend.service.SmartSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProductController {

    private final ProductService productService;
    // private final SmartSearchService smartSearchService;

    @GetMapping
    public List<Product> getAll() {
        return productService.getActiveProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productService.getActiveProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    // @GetMapping("/search")
    // public List<Product> search(@RequestParam String q) {
    //     return smartSearchService.search(q);
    // }
}