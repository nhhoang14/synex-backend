package com.nhhoang.synexbackend.controller.admin;

import com.nhhoang.synexbackend.dto.request.UpdateVariantPriceRequest;
import com.nhhoang.synexbackend.dto.request.UpsertProductVariantRequest;
import com.nhhoang.synexbackend.entity.ProductVariant;
import com.nhhoang.synexbackend.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products/{productId}/variants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin("*")
public class AdminProductVariantController {

    private final ProductVariantService productVariantService;

    @GetMapping
    public ResponseEntity<List<ProductVariant>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productVariantService.getByProductId(productId));
    }

    @PostMapping
    public ResponseEntity<ProductVariant> create(@PathVariable Long productId,
                                                 @RequestBody UpsertProductVariantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productVariantService.create(productId, request));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ProductVariant>> createBulk(@PathVariable Long productId,
                                                          @RequestBody List<UpsertProductVariantRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productVariantService.createBulk(productId, requests));
    }

    @PutMapping("/{variantId}")
    public ResponseEntity<ProductVariant> update(@PathVariable Long productId,
                                                 @PathVariable Long variantId,
                                                 @RequestBody UpsertProductVariantRequest request) {
        return ResponseEntity.ok(productVariantService.update(productId, variantId, request));
    }

    @PatchMapping("/{variantId}/price")
    public ResponseEntity<ProductVariant> updatePrice(@PathVariable Long productId,
                                                      @PathVariable Long variantId,
                                                      @RequestBody UpdateVariantPriceRequest request) {
        return ResponseEntity.ok(productVariantService.updatePrice(productId, variantId, request));
    }

    @DeleteMapping("/{variantId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId,
                                       @PathVariable Long variantId) {
        productVariantService.delete(productId, variantId);
        return ResponseEntity.noContent().build();
    }
}
