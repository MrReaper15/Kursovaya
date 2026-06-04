package com.buildingstore.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Валюта отображения цен. Базовая валюта хранения — российский рубль (RUB).
 * Цены в каталоге заданы в рублях РФ и при необходимости конвертируются.
 *
 * Курс задан фиксированно (для учебного прототипа): 1 BYN ≈ 30 RUB.
 */
public enum Currency {

    /** Российский рубль — базовая валюта. */
    RUB("₽", new BigDecimal("1")),
    /** Белорусский рубль. 1 BYN = 30 RUB. */
    BYN("Br", new BigDecimal("30"));

    private final String symbol;
    private final BigDecimal rubPerUnit;

    Currency(String symbol, BigDecimal rubPerUnit) {
        this.symbol = symbol;
        this.rubPerUnit = rubPerUnit;
    }

    public String getCode() {
        return name();
    }

    public String getSymbol() {
        return symbol;
    }

    /** Перевести сумму из базовых рублей в эту валюту. */
    public BigDecimal convert(BigDecimal rub) {
        if (rub == null) {
            return BigDecimal.ZERO;
        }
        if (this == RUB) {
            return rub;
        }
        return rub.divide(rubPerUnit, 2, RoundingMode.HALF_UP);
    }

    /** Конвертировать и отформатировать (без символа). Удобно для JSP: ${cur.format(price)}. */
    public String format(BigDecimal rub) {
        return Money.format(convert(rub));
    }

    /** Разбор кода валюты; при ошибке — рубль РФ. */
    public static Currency from(String code) {
        if (code != null) {
            for (Currency c : values()) {
                if (c.name().equalsIgnoreCase(code.trim())) {
                    return c;
                }
            }
        }
        return RUB;
    }
}
