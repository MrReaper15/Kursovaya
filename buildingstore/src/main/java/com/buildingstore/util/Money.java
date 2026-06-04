package com.buildingstore.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Форматирование чисел: разделитель тысяч — пробел, десятичный — запятая
 * (как принято в РФ и РБ). Пример: 12500 -> «12 500», 17.33 -> «17,33».
 * Символ валюты добавляется отдельно во вью (см. тег price.tag).
 */
public final class Money {

    private Money() {}

    private static final DecimalFormatSymbols SYMBOLS;
    static {
        SYMBOLS = new DecimalFormatSymbols(Locale.ROOT);
        SYMBOLS.setGroupingSeparator(' ');
        SYMBOLS.setDecimalSeparator(',');
    }

    /** DecimalFormat не потокобезопасен, поэтому создаём его на каждый вызов. */
    public static String format(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return new DecimalFormat("#,##0.##", SYMBOLS).format(value);
    }
}
