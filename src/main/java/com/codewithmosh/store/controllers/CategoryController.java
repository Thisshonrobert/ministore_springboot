//member 1
package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.*;
import com.codewithmosh.store.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;

    @RestController @RequestMapping("/categories") @RequiredArgsConstructor
    public class CategoryController {
        private final CategoryService service;
        @GetMapping public List<CategoryDto> list() { return service.list(); }
        @GetMapping("/{id}") public CategoryDto get(@PathVariable Byte id) { return service.get(id); }
        @PostMapping public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryRequest request) {
            var dto = service.create(request);
            return ResponseEntity.created(URI.create("/categories/" + dto.getId())).body(dto);
        }
        @PutMapping("/{id}") public CategoryDto update(@PathVariable Byte id, @Valid @RequestBody CategoryRequest request) { return service.update(id, request); }
        @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Byte id) {
            service.delete(id);
            return ResponseEntity.noContent().build();
        }
    }
