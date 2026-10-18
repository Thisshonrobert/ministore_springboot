//member 1
package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.CategoryDto;
import com.codewithmosh.store.dtos.CategoryRequest;
import com.codewithmosh.store.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryDto> list() {
        return categoryService.list();
    }

    @GetMapping("/{id}")
    public CategoryDto get(@PathVariable Byte id) {
        return categoryService.get(id);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryRequest request) {
        CategoryDto dto = categoryService.create(request);
        URI location = URI.create("/categories/" + dto.getId());
        return ResponseEntity.created(location).body(dto);
    }

    @PutMapping("/{id}")
    public CategoryDto update(@PathVariable Byte id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Byte id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
