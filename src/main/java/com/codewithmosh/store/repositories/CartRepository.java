//member 4
package com.codewithmosh.store.repositories;

import com.codewithmosh.store.entities.Cart;
        import org.springframework.data.jpa.repository.JpaRepository;
        public interface CartRepository extends JpaRepository<Cart, java.util.UUID> {
            java.util.Optional<Cart> findByUserId(Long userId);
boolean existsByUserId(Long userId);
@org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
@org.springframework.data.jpa.repository.Query("select c from Cart c where c.id = :id")
java.util.Optional<Cart> findForUpdate(@org.springframework.data.repository.query.Param("id") java.util.UUID id);
        }
