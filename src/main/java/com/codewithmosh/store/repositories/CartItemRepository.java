//member 4
package com.codewithmosh.store.repositories;

import com.codewithmosh.store.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    boolean existsByProductId(Long productId);
}
