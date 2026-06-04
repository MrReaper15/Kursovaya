package com.buildingstore.servlet;

import com.buildingstore.data.Catalog;
import com.buildingstore.model.Product;
import com.buildingstore.util.Currency;
import com.buildingstore.util.WebUtils;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Подсказки для живого поиска. GET /api/suggest?q=...
 * Возвращает JSON-массив до 6 товаров: [{id, name, category, price}].
 */
public class SuggestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int LIMIT = 6;

    private final Catalog catalog = Catalog.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String query = req.getParameter("q");
        Currency currency = WebUtils.getCurrency(req);

        StringBuilder json = new StringBuilder(256);
        json.append('[');

        if (query != null && query.trim().length() >= 2) {
            List<Product> found = catalog.search(query);
            int count = Math.min(LIMIT, found.size());
            for (int i = 0; i < count; i++) {
                Product p = found.get(i);
                if (i > 0) {
                    json.append(',');
                }
                String price = currency.format(p.getPrice()) + " " + currency.getSymbol();
                json.append('{')
                        .append("\"id\":").append(p.getId()).append(',')
                        .append("\"name\":\"").append(escape(p.getName())).append("\",")
                        .append("\"category\":\"")
                        .append(escape(p.getCategory() != null ? p.getCategory().getName() : "")).append("\",")
                        .append("\"price\":\"").append(escape(price)).append('"')
                        .append('}');
            }
        }

        json.append(']');

        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();
    }

    /** Экранирование строки для JSON. */
    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder b = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
                }
            }
        }
        return b.toString();
    }
}
