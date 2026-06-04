package com.buildingstore.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Позиция корзины: товар + количество. Хранится в сессии, поэтому Serializable.
 */
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = Math.max(1, quantity);
    }

    public Product getProduct() { return product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = Math.max(1, quantity); }

    /** Сумма по позиции = цена × количество (в базовых рублях). */
    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
