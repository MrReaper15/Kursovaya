package com.buildingstore.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Корзина покупок. Хранится в HttpSession, поэтому Serializable.
 * Вся логика добавления/изменения/удаления товаров инкапсулирована здесь.
 */
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<CartItem> items = new ArrayList<>();

    /** Неизменяемое представление позиций — чтобы JSP/контроллеры не меняли список напрямую. */
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Добавить товар. Если он уже в корзине — увеличиваем количество. */
    public void add(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return;
        }
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(product, quantity));
    }

    /** Установить точное количество. Если <= 0 — позиция удаляется. */
    public void update(Long productId, int quantity) {
        if (productId == null) {
            return;
        }
        if (quantity <= 0) {
            remove(productId);
            return;
        }
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
                return;
            }
        }
    }

    public void remove(Long productId) {
        if (productId == null) {
            return;
        }
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** Общее количество единиц товара во всех позициях. */
    public int getTotalCount() {
        int sum = 0;
        for (CartItem item : items) {
            sum += item.getQuantity();
        }
        return sum;
    }

    /** Итоговая сумма заказа (в базовых рублях). */
    public BigDecimal getTotalPrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }
}
