//member 4
package com.accenture.store.dtos;

import java.util.List;

/**
 * Everything the storefront needs for one product's review section: the real
 * reviews, their average, the cached AI summary when one exists and a short
 * explanation when a summary is not possible yet.
 */
public class ProductReviewsDto {
    private Long productId;
    private String productName;
    private int reviewCount;
    private Double averageRating;
    private List<ReviewDto> reviews;
    private ReviewSummaryDto summary;
    private String summaryMessage;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public List<ReviewDto> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDto> reviews) {
        this.reviews = reviews;
    }

    public ReviewSummaryDto getSummary() {
        return summary;
    }

    public void setSummary(ReviewSummaryDto summary) {
        this.summary = summary;
    }

    public String getSummaryMessage() {
        return summaryMessage;
    }

    public void setSummaryMessage(String summaryMessage) {
        this.summaryMessage = summaryMessage;
    }
}
