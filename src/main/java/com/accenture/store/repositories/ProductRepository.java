//member 2
package com.accenture.store.repositories;

import com.accenture.store.entities.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Byte categoryId);

    boolean existsByCategoryId(Byte categoryId);

    /**
     * The catalogue the shopping assistant grounds its answers in. The category
     * is fetched in the same query because open-in-view is switched off, and the
     * Pageable keeps a single prompt a sane size.
     */
    @Query("select p from Product p join fetch p.category order by p.id")
    List<Product> findCatalogue(Pageable pageable);

    /**
     * Keyword lookup for the shopping assistant, so the chat answers come from
     * real rows instead of a text guess. The caller passes a lower-cased
     * "%keyword%" pattern and limits the result with a Pageable.
     */
    @Query("""
            select p from Product p
            join fetch p.category c
            where lower(p.name) like :pattern
               or lower(p.description) like :pattern
               or lower(c.name) like :pattern
            order by p.id
            """)
    List<Product> search(@Param("pattern") String pattern, Pageable pageable);
}
