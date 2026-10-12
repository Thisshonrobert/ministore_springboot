//member 1
package com.codewithmosh.store.services;

import com.codewithmosh.store.entities.*;
import com.codewithmosh.store.dtos.*;
import com.codewithmosh.store.repositories.*;
import com.codewithmosh.store.mappers.*;
import com.codewithmosh.store.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class CategoryService {
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final CategoryMapper mapper;
    @Transactional(readOnly = true)
    public List<CategoryDto> list() { return categories.findAll().stream().map(mapper::toDto).toList(); }
    @Transactional(readOnly = true)
    public CategoryDto get(Byte id) { return mapper.toDto(find(id)); }
    public CategoryDto create(CategoryRequest request) { return mapper.toDto(categories.save(mapper.toEntity(request))); }
    public CategoryDto update(Byte id, CategoryRequest request) {
        var category = find(id);
        mapper.update(request, category);
        return mapper.toDto(category);
    }
    public void delete(Byte id) {
        var category = find(id);
        if (products.existsByCategoryId(id)) throw new ConflictException("Delete this category's products first.");
        categories.delete(category);
    }
    private Category find(Byte id) {
        return categories.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }
}
