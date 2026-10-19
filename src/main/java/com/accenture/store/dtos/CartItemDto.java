//member 4
package com.accenture.store.dtos;

import java.math.BigDecimal;

public class CartItemDto {
    private CartProductDto product;
    private int quantity;
    private BigDecimal totalPrice;

    public CartProductDto getProduct() {
        return product;
    }

    public void setProduct(CartProductDto product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
