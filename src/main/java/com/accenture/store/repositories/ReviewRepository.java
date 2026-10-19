//member 4
package com.accenture.store.repositories;

import com.accenture.store.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    /**
     * Reviews for one product, newest first. The author is fetched with the
     * review so the DTO can be built without a session (lazy loading would
     * otherwise fail, because open-in-view is switched off).
     */
    @Query("""
            select r from Review r
            join fetch r.user
            where r.product.id = :productId
            order by r.createdAt desc, r.id desc
            """)
    List<Review> findForProduct(@Param("productId") Long productId);

    boolean existsByProductId(Long productId);

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    void deleteByProductId(Long productId);
}
