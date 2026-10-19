//member 2
package com.accenture.store.mappers;

import com.accenture.store.dtos.ProductDto;
import com.accenture.store.dtos.ProductRequest;
import com.accenture.store.entities.Category;
import com.accenture.store.entities.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());

        Category category = product.getCategory();
        if (category != null) {
            dto.setCategoryId(category.getId());
        }

        return dto;
    }

    public Product toEntity(ProductRequest request) {
        if (request == null) {
            return null;
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        return product;
    }

    public void update(ProductRequest request, Product product) {
        if (request == null) {
            return;
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
    }
}
