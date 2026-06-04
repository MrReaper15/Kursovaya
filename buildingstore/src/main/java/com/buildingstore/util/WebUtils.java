package com.buildingstore.util;

import com.buildingstore.model.Cart;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Вспомогательные методы для сервлетов: работа с корзиной в сессии,
 * flash-сообщения (для паттерна Post/Redirect/Get) и безопасный разбор чисел.
 */
public final class WebUtils {

    private WebUtils() {}

    public static final String CART_ATTR = "cart";
    public static final String CURRENCY_COOKIE = "cur";
    private static final String FLASH_MESSAGE = "flash.message";
    private static final String FLASH_ORDER = "flash.order";

    /** Текущая валюта отображения из cookie (по умолчанию рубль РФ). */
    public static Currency getCurrency(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (CURRENCY_COOKIE.equals(cookie.getName())) {
                    return Currency.from(cookie.getValue());
                }
            }
        }
        return Currency.RUB;
    }

    /** URL-кодирование значения для подстановки в ссылку (например, параметр back). */
    public static String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    /** Возвращает корзину текущей сессии, создавая её при необходимости. */
    public static Cart getCart(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Cart cart = (Cart) session.getAttribute(CART_ATTR);
        if (cart == null) {
            cart = new Cart();
            session.setAttribute(CART_ATTR, cart);
        }
        return cart;
    }

    /** Сохранить flash-сообщение «товар добавлен» и т.п. до следующего запроса. */
    public static void setFlashMessage(HttpServletRequest request, String message) {
        request.getSession(true).setAttribute(FLASH_MESSAGE, message);
    }

    /** Сохранить флаг успешного оформления заказа (с именем клиента). */
    public static void setFlashOrder(HttpServletRequest request, String clientName) {
        request.getSession(true).setAttribute(FLASH_ORDER, clientName);
    }

    /**
     * Переносит flash-атрибуты из сессии в атрибуты запроса (для JSP) и удаляет их,
     * чтобы сообщение показалось ровно один раз.
     */
    public static void consumeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object message = session.getAttribute(FLASH_MESSAGE);
        if (message != null) {
            request.setAttribute("message", message);
            session.removeAttribute(FLASH_MESSAGE);
        }
        Object order = session.getAttribute(FLASH_ORDER);
        if (order != null) {
            request.setAttribute("orderSuccess", Boolean.TRUE);
            request.setAttribute("clientName", order);
            session.removeAttribute(FLASH_ORDER);
        }
    }

    /** Разбор целого числа с значением по умолчанию (без выброса исключений). */
    public static int parseInt(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Разбор Long из строки; возвращает null при ошибке. */
    public static Long parseLong(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Защита от open-redirect: разрешаем перенаправление только на локальные пути,
     * начинающиеся с одного «/». Иначе возвращаем «/».
     */
    public static String safeLocalRedirect(String target) {
        if (target == null || target.isBlank()) {
            return "/";
        }
        if (target.startsWith("/") && !target.startsWith("//")) {
            return target;
        }
        return "/";
    }
}
