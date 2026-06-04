package com.buildingstore.servlet;

import com.buildingstore.util.Currency;
import com.buildingstore.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Переключение валюты отображения. Адрес: GET /currency?c=BYN&back=/путь
 * Сохраняет выбор в cookie на год и возвращает пользователя на ту же страницу.
 */
public class CurrencyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Currency currency = Currency.from(req.getParameter("c"));

        Cookie cookie = new Cookie(WebUtils.CURRENCY_COOKIE, currency.name());
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 365); // 1 год
        cookie.setHttpOnly(true);
        resp.addCookie(cookie);

        String back = WebUtils.safeLocalRedirect(req.getParameter("back"));
        resp.sendRedirect(req.getContextPath() + back);
    }
}
