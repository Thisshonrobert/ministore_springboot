//member 3
package com.codewithmosh.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

    @Getter @Setter @Entity @Table(name = "users")
    public class User {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @Column(nullable = false, length = 100)
        private String name;
        @Column(nullable = false, unique = true)
        private String email;
    }
