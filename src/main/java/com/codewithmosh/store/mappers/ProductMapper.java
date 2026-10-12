//member 2
package com.codewithmosh.store.mappers;

import com.codewithmosh.store.entities.Product;
    import com.codewithmosh.store.dtos.*;
    import org.mapstruct.*;
    @Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
    public interface ProductMapper {
        @Mapping(target = "categoryId", source = "category.id")
        ProductDto toDto(Product entity);
        @Mapping(target = "id", ignore = true)
@Mapping(target = "category", ignore = true)
        Product toEntity(ProductRequest request);
        @Mapping(target = "id", ignore = true)
@Mapping(target = "category", ignore = true)
        void update(ProductRequest request, @MappingTarget Product entity);
    }
