//member 1
package com.codewithmosh.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

    @Getter @Setter @Entity @Table(name = "categories")
    public class Category {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Byte id;
        @Column(nullable = false, length = 100)
        private String name;
        @OneToMany(mappedBy = "category")
        private java.util.Set<Product> products = new java.util.HashSet<>();
    }
