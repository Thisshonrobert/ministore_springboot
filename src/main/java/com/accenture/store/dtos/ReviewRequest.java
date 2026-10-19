//member 4
package com.accenture.store.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A review submitted by a signed-in customer. The customer is taken from the
 * session, so only the product and the customer's own words travel here.
 */
public class ReviewRequest {
    @NotNull(message = "Choose the product you are reviewing.")
    private Long productId;

    @NotNull(message = "Choose a rating from 1 to 5.")
    @Min(value = 1, message = "Ratings run from 1 to 5.")
    @Max(value = 5, message = "Ratings run from 1 to 5.")
    private Integer rating;

    @NotBlank(message = "Write a few words about the product.")
    @Size(max = 1000, message = "Reviews must be 1000 characters or fewer.")
    private String comment;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
