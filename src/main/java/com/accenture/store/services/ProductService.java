//member 2
package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.ProductDto;
import com.codewithmosh.store.dtos.ProductRequest;
import com.codewithmosh.store.entities.Category;
import com.codewithmosh.store.entities.Product;
import com.codewithmosh.store.exceptions.ConflictException;
import com.codewithmosh.store.exceptions.ResourceNotFoundException;
import com.codewithmosh.store.mappers.ProductMapper;
import com.codewithmosh.store.repositories.CartItemRepository;
import com.codewithmosh.store.repositories.CategoryRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          CartItemRepository cartItemRepository,
                          ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cartItemRepository = cartItemRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> list(Byte categoryId) {
        List<Product> products;
        if (categoryId == null) {
            products = productRepository.findAll();
        } else {
            products = productRepository.findByCategoryId(categoryId);
        }

        List<ProductDto> result = new ArrayList<>();
        for (Product product : products) {
            result.add(productMapper.toDto(product));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public ProductDto get(Long id) {
        Product product = findProduct(id);
        return productMapper.toDto(product);
    }

    public ProductDto create(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        product.setCategory(findCategory(request.getCategoryId()));
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    public ProductDto update(Long id, ProductRequest request) {
        Product product = findProduct(id);
        productMapper.update(request, product);
        product.setCategory(findCategory(request.getCategoryId()));
        return productMapper.toDto(product);
    }

    public void delete(Long id) {
        Product product = findProduct(id);

        if (cartItemRepository.existsByProductId(id)) {
            throw new ConflictException("Remove this product from all carts first.");
        }

        productRepository.delete(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
    }

    private Category findCategory(Byte id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }
}
