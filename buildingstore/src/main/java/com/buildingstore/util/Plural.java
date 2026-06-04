package com.buildingstore.util;

/**
 * Правильное склонение существительных после числа в русском языке.
 * Пример: Plural.ru(1, "товар", "товара", "товаров") -> "1 товар",
 *         Plural.ru(3, ...) -> "3 товара", Plural.ru(12, ...) -> "12 товаров".
 */
public final class Plural {

    private Plural() {}

    public static String ru(long number, String one, String few, String many) {
        long n10 = Math.abs(number) % 10;
        long n100 = Math.abs(number) % 100;

        String word;
        if (n10 == 1 && n100 != 11) {
            word = one;
        } else if (n10 >= 2 && n10 <= 4 && !(n100 >= 12 && n100 <= 14)) {
            word = few;
        } else {
            word = many;
        }
        return number + " " + word;
    }
}
