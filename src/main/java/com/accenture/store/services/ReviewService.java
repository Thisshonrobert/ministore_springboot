//member 4
package com.accenture.store.services;

import com.accenture.store.ai.AiClient;
import com.accenture.store.ai.AiPrompts;
import com.accenture.store.dtos.ProductReviewsDto;
import com.accenture.store.dtos.ReviewDto;
import com.accenture.store.dtos.ReviewRequest;
import com.accenture.store.dtos.ReviewSummaryDto;
import com.accenture.store.entities.Product;
import com.accenture.store.entities.Review;
import com.accenture.store.entities.ReviewSummary;
import com.accenture.store.entities.User;
import com.accenture.store.exceptions.ConflictException;
import com.accenture.store.exceptions.ResourceNotFoundException;
import com.accenture.store.exceptions.UnauthorizedException;
import com.accenture.store.mappers.ReviewMapper;
import com.accenture.store.repositories.ProductRepository;
import com.accenture.store.repositories.ReviewRepository;
import com.accenture.store.repositories.ReviewSummaryRepository;
import com.accenture.store.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ratings, written reviews and the cached AI summary of them.
 *
 * The customer always comes from the session, never from the request body. A
 * new review removes the cached summary, so the next summary is generated from
 * the reviews that actually exist.
 */
@Service
public class ReviewService {
    /** How many of the newest reviews are sent to the AI. Keeps the prompt small. */
    private static final int REVIEWS_PER_SUMMARY = 10;
    /** Matches the width of review_summary.content in the database. */
    private static final int MAX_SUMMARY_CHARACTERS = 2000;

    private final ReviewRepository reviewRepository;
    private final ReviewSummaryRepository reviewSummaryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final AiClient aiClient;
    private final AiPrompts aiPrompts;

    private final int minimumReviewsForSummary;
    private final int summaryTtlHours;

    public ReviewService(ReviewRepository reviewRepository,
                         ReviewSummaryRepository reviewSummaryRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository,
                         ReviewMapper reviewMapper,
                         AiClient aiClient,
                         AiPrompts aiPrompts,
                         @Value("${app.reviews.minimum-for-summary:3}") int minimumReviewsForSummary,
                         @Value("${app.reviews.summary-ttl-hours:168}") int summaryTtlHours) {
        this.reviewRepository = reviewRepository;
        this.reviewSummaryRepository = reviewSummaryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.reviewMapper = reviewMapper;
        this.aiClient = aiClient;
        this.aiPrompts = aiPrompts;
        this.minimumReviewsForSummary = minimumReviewsForSummary;
        this.summaryTtlHours = summaryTtlHours;
    }

    @Transactional
    public ReviewDto createReview(Long productId, Long customerId, ReviewRequest request) {
        Product product = findProduct(productId);
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedException("Your session is no longer valid. Please sign in again."));

        if (reviewRepository.existsByProductIdAndUserId(productId, customerId)) {
            throw new ConflictException("You have already reviewed this product.");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(customer);
        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());
        Review saved = reviewRepository.save(review);

        // The old summary described the old set of reviews, so it is dropped
        // rather than served as if it were still current.
        reviewSummaryRepository.deleteByProductId(productId);

        return reviewMapper.toDto(saved);
    }

    public ProductReviewsDto getProductReviews(Long productId) {
        Product product = findProduct(productId);
        List<Review> reviews = reviewRepository.findForProduct(productId);
        return toDto(product, reviews, usableSummary(productId), null);
    }

    /**
     * Returns the cached summary when it is still valid and complete enough,
     * and otherwise asks the AI for a fresh one and caches it. Products with no
     * reviews, or with fewer reviews than the configured minimum, are reported
     * as a normal response with an explanation instead of an error.
     */
    public ProductReviewsDto summarize(Long productId) {
        Product product = findProduct(productId);
        List<Review> reviews = reviewRepository.findForProduct(productId);

        ReviewSummary cached = usableSummary(productId);
        if (cached != null) {
            return toDto(product, reviews, cached, null);
        }

        if (reviews.size() < minimumReviewsForSummary) {
            return toDto(product, reviews, null, notEnoughReviewsMessage(reviews.size()));
        }

        List<Review> sample = reviews.size() > REVIEWS_PER_SUMMARY
                ? reviews.subList(0, REVIEWS_PER_SUMMARY)
                : reviews;

        String content = aiClient.generate(aiPrompts.summarizeReviews(), describeReviews(product, reviews, sample));
        if (content.length() > MAX_SUMMARY_CHARACTERS) {
            content = content.substring(0, MAX_SUMMARY_CHARACTERS).trim();
        }

        LocalDateTime now = LocalDateTime.now();
        ReviewSummary summary = reviewSummaryRepository.findByProductId(productId).orElseGet(ReviewSummary::new);
        summary.setProduct(product);
        summary.setContent(content);
        summary.setGeneratedAt(now);
        summary.setExpiresAt(now.plusHours(summaryTtlHours));
        ReviewSummary saved = reviewSummaryRepository.save(summary);

        return toDto(product, reviews, saved, null);
    }

    private ReviewSummary usableSummary(Long productId) {
        LocalDateTime now = LocalDateTime.now();
        ReviewSummary summary = reviewSummaryRepository.findByProductId(productId).orElse(null);
        return summary != null && summary.isUsableAt(now) ? summary : null;
    }

    private String describeReviews(Product product, List<Review> reviews, List<Review> sample) {
        StringBuilder text = new StringBuilder();
        text.append("Product: ").append(product.getName()).append('\n');
        text.append("Reviews in total: ").append(reviews.size())
                .append(". Average rating: ").append(averageRating(reviews)).append(" out of 5.\n");
        text.append("Reviews to summarise, newest first:\n");

        for (Review review : sample) {
            text.append("- ").append(review.getRating()).append("/5: ").append(review.getComment()).append('\n');
        }

        return text.toString();
    }

    private String notEnoughReviewsMessage(int reviewCount) {
        String needed = minimumReviewsForSummary + (minimumReviewsForSummary == 1 ? " review" : " reviews");
        if (reviewCount == 0) {
            return "This product has no reviews yet, so there is nothing to summarise. "
                    + "It needs " + needed + " before a summary can be written.";
        }
        return "There are only " + reviewCount + " review" + (reviewCount == 1 ? "" : "s")
                + " so far. A summary needs " + needed + ".";
    }

    private ProductReviewsDto toDto(Product product,
                                    List<Review> reviews,
                                    ReviewSummary summary,
                                    String summaryMessage) {
        ProductReviewsDto dto = new ProductReviewsDto();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setReviewCount(reviews.size());
        dto.setAverageRating(averageRating(reviews));

        List<ReviewDto> reviewDtos = new ArrayList<>();
        for (Review review : reviews) {
            reviewDtos.add(reviewMapper.toDto(review));
        }
        dto.setReviews(reviewDtos);
        dto.setSummary(toSummaryDto(summary));
        dto.setSummaryMessage(summaryMessage);
        return dto;
    }

    private ReviewSummaryDto toSummaryDto(ReviewSummary summary) {
        if (summary == null) {
            return null;
        }

        ReviewSummaryDto dto = new ReviewSummaryDto();
        dto.setContent(summary.getContent());
        dto.setGeneratedAt(summary.getGeneratedAt());
        dto.setExpiresAt(summary.getExpiresAt());
        return dto;
    }

    private Double averageRating(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return null;
        }

        int total = 0;
        for (Review review : reviews) {
            total += review.getRating();
        }

        return Math.round(total / (double) reviews.size() * 10) / 10.0;
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
    }
}
