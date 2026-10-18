//member 1
package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.CategoryDto;
import com.codewithmosh.store.dtos.CategoryRequest;
import com.codewithmosh.store.entities.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }

    public Category toEntity(CategoryRequest request) {
        if (request == null) {
            return null;
        }

        Category category = new Category();
        category.setName(request.getName());
        return category;
    }

    public void update(CategoryRequest request, Category category) {
        if (request == null) {
            return;
        }

        category.setName(request.getName());
    }
}
