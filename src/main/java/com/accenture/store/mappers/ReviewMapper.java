//member 4
package com.accenture.store.mappers;

import com.accenture.store.dtos.ReviewDto;
import com.accenture.store.entities.Review;
import com.accenture.store.entities.User;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public ReviewDto toDto(Review review) {
        if (review == null) {
            return null;
        }

        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        User author = review.getUser();
        if (author != null) {
            // Only the display name is exposed; the email address stays private.
            dto.setAuthorName(author.getName());
        }

        return dto;
    }
}
