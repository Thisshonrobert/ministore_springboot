//member 4
package com.accenture.store.repositories;

import com.accenture.store.entities.ReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewSummaryRepository extends JpaRepository<ReviewSummary, Long> {
    Optional<ReviewSummary> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}
