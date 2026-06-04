package com.buildingstore.model;

import java.math.BigDecimal;

/**
 * Товар магазина. Обычный POJO (без ORM) — данные хранятся в памяти.
 */
public class Product {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String unit;     // шт, кг, м², мешок и т.д.
    private Category category;

    public Product() {}

    public Product(Long id, String name, String description, BigDecimal price,
                   Integer stock, String unit, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.unit = unit;
        this.category = category;
    }

    /** Есть ли товар в наличии. */
    public boolean isInStock() {
        return stock != null && stock > 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
