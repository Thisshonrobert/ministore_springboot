//member 4
package com.accenture.store.dtos;

import java.util.List;

/**
 * The assistant's answer plus the catalogue rows the answer was grounded in, so
 * the storefront can show the same products the backend looked up.
 */
public class ChatResponse {
    private String reply;
    private List<ProductDto> products;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<ProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDto> products) {
        this.products = products;
    }
}
