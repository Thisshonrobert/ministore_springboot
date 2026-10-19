//member 2
package com.accenture.store.services;

import com.accenture.store.dtos.ProductDto;
import com.accenture.store.dtos.ProductRequest;
import com.accenture.store.entities.Category;
import com.accenture.store.entities.Product;
import com.accenture.store.exceptions.ConflictException;
import com.accenture.store.exceptions.ResourceNotFoundException;
import com.accenture.store.mappers.ProductMapper;
import com.accenture.store.repositories.CartItemRepository;
import com.accenture.store.repositories.CategoryRepository;
import com.accenture.store.repositories.ProductRepository;
import com.accenture.store.repositories.ReviewRepository;
import com.accenture.store.repositories.ReviewSummaryRepository;
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
    private final ReviewRepository reviewRepository;
    private final ReviewSummaryRepository reviewSummaryRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          CartItemRepository cartItemRepository,
                          ReviewRepository reviewRepository,
                          ReviewSummaryRepository reviewSummaryRepository,
                          ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cartItemRepository = cartItemRepository;
        this.reviewRepository = reviewRepository;
        this.reviewSummaryRepository = reviewSummaryRepository;
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

        // A product's reviews and their cached summary belong to the product, so
        // they go with it instead of blocking the delete.
        reviewSummaryRepository.deleteByProductId(id);
        reviewRepository.deleteByProductId(id);
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
