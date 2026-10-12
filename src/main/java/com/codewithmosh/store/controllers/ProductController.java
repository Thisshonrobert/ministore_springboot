//member 2
package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.*;
import com.codewithmosh.store.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;

    @RestController @RequestMapping("/products") @RequiredArgsConstructor
    public class ProductController {
        private final ProductService service;
        @GetMapping public List<ProductDto> list(@RequestParam(required = false) Byte categoryId) { return service.list(categoryId); }
        @GetMapping("/{id}") public ProductDto get(@PathVariable Long id) { return service.get(id); }
        @PostMapping public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductRequest request) {
            var dto = service.create(request);
            return ResponseEntity.created(URI.create("/products/" + dto.getId())).body(dto);
        }
        @PutMapping("/{id}") public ProductDto update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) { return service.update(id, request); }
        @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
            service.delete(id);
            return ResponseEntity.noContent().build();
        }
    }
