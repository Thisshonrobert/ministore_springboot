//member 4
package com.accenture.store.controllers;

import com.accenture.store.dtos.ProductReviewsDto;
import com.accenture.store.dtos.ReviewDto;
import com.accenture.store.dtos.ReviewRequest;
import com.accenture.store.services.AuthService;
import com.accenture.store.services.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Reading reviews and their AI summary is public, like the product catalogue.
 * Writing a review lives under /me, where the AuthInterceptor and the
 * customer's session decide who the author is.
 */
@RestController
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/products/{productId}/reviews")
    public ProductReviewsDto list(@PathVariable Long productId) {
        return reviewService.getProductReviews(productId);
    }

    /**
     * Generates (or returns the cached) summary of this product's reviews. A
     * product with no reviews is a normal answer, not an error.
     */
    @PostMapping("/products/{productId}/reviews/summary")
    public ProductReviewsDto summarize(@PathVariable Long productId) {
        return reviewService.summarize(productId);
    }

    @PostMapping("/me/reviews")
    public ResponseEntity<ReviewDto> create(HttpSession session, @Valid @RequestBody ReviewRequest request) {
        Long customerId = (Long) session.getAttribute(AuthService.SESSION_CUSTOMER_ID);
        ReviewDto review = reviewService.createReview(request.getProductId(), customerId, request);
        return ResponseEntity.created(URI.create("/products/" + request.getProductId() + "/reviews")).body(review);
    }
}
