//member 2
package com.codewithmosh.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

    import java.math.BigDecimal;
    @Getter @Setter @Entity @Table(name = "products")
    public class Product {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @Column(nullable = false, length = 100)
        private String name;
        @Column(length = 1000)
        private String description;
        @Column(nullable = false, precision = 10, scale = 2)
        private BigDecimal price;
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "category_id", nullable = false)
        private Category category;
    }
