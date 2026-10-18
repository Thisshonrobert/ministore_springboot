//member 1
package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.CategoryDto;
import com.codewithmosh.store.dtos.CategoryRequest;
import com.codewithmosh.store.entities.Category;
import com.codewithmosh.store.exceptions.ConflictException;
import com.codewithmosh.store.exceptions.ResourceNotFoundException;
import com.codewithmosh.store.mappers.CategoryMapper;
import com.codewithmosh.store.repositories.CategoryRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> list() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDto> result = new ArrayList<>();

        for (Category category : categories) {
            result.add(categoryMapper.toDto(category));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public CategoryDto get(Byte id) {
        Category category = findCategory(id);
        return categoryMapper.toDto(category);
    }

    public CategoryDto create(CategoryRequest request) {
        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    public CategoryDto update(Byte id, CategoryRequest request) {
        Category category = findCategory(id);
        categoryMapper.update(request, category);
        return categoryMapper.toDto(category);
    }

    public void delete(Byte id) {
        Category category = findCategory(id);

        if (productRepository.existsByCategoryId(id)) {
            throw new ConflictException("Delete this category's products first.");
        }

        categoryRepository.delete(category);
    }

    private Category findCategory(Byte id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }
}
