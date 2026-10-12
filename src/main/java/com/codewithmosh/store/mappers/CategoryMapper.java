//member 1
package com.codewithmosh.store.mappers;

import com.codewithmosh.store.entities.Category;
    import com.codewithmosh.store.dtos.*;
    import org.mapstruct.*;
    @Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
    public interface CategoryMapper {

        CategoryDto toDto(Category entity);
        @Mapping(target = "id", ignore = true)
@Mapping(target = "products", ignore = true)
        Category toEntity(CategoryRequest request);
        @Mapping(target = "id", ignore = true)
@Mapping(target = "products", ignore = true)
        void update(CategoryRequest request, @MappingTarget Category entity);
    }
