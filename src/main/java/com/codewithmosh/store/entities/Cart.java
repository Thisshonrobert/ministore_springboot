//member 4
package com.codewithmosh.store.entities;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "carts")
public class Cart {
    @Id

    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 16)
    private UUID id;

    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDate dateCreated = LocalDate.now();

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart" , cascade = CascadeType.ALL , fetch = FetchType.LAZY , orphanRemoval = true)
    private Set<CartItem> items = new LinkedHashSet<>();

    public BigDecimal getTotalPrice(){
       return  items.stream().map(item-> item.getTotalPrice()).
                reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public CartItem getItem(Long productId){

        return items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    public CartItem addItem(Product product) {
        var cartItem = getItem(product.getId());
        if(cartItem != null) cartItem.setQuantity(cartItem.getQuantity()+1);
        else{
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
            cartItem.setCart(this);
           items.add(cartItem);
        }
        return cartItem;
    }

    public void removeItem(Long productId){
        var cartItem = getItem(productId);

        if(cartItem!=null){
            items.remove(cartItem);
        }
    }


    public void clear() {
        items.clear();
    }
}
