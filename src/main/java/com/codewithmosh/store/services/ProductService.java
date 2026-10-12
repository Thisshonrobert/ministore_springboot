//member 2
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
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final CartItemRepository items;
    private final ProductMapper mapper;
    @Transactional(readOnly = true)
    public List<ProductDto> list(Byte categoryId) {
        return (categoryId == null ? products.findAll() : products.findByCategoryId(categoryId)).stream().map(mapper::toDto).toList();
    }
    @Transactional(readOnly = true)
    public ProductDto get(Long id) { return mapper.toDto(find(id)); }
    public ProductDto create(ProductRequest request) {
        var product = mapper.toEntity(request);
        product.setCategory(category(request.getCategoryId()));
        return mapper.toDto(products.save(product));
    }
    public ProductDto update(Long id, ProductRequest request) {
        var product = find(id);
        mapper.update(request, product);
        product.setCategory(category(request.getCategoryId()));
        return mapper.toDto(product);
    }
    public void delete(Long id) {
        var product = find(id);
        if (items.existsByProductId(id)) throw new ConflictException("Remove this product from all carts first.");
        products.delete(product);
    }
    private Product find(Long id) {
        return products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found."));
    }
    private Category category(Byte id) {
        return categories.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }
}
