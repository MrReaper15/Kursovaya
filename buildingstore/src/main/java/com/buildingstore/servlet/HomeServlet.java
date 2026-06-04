package com.buildingstore.servlet;

import com.buildingstore.data.Catalog;
import com.buildingstore.model.Cart;
import com.buildingstore.model.Product;
import com.buildingstore.util.Images;
import com.buildingstore.util.Plural;
import com.buildingstore.util.WebUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

/**
 * Главная страница: каталог товаров, поиск и фильтр по категории.
 * Отображается по корневому адресу «/».
 */
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final Catalog catalog = Catalog.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String search = trimToNull(req.getParameter("search"));
        String category = trimToNull(req.getParameter("category"));

        List<Product> products;
        String currentUrl;

        if (search != null) {
            products = catalog.search(search);
            req.setAttribute("searchQuery", search);
            currentUrl = "/?search=" + URLEncoder.encode(search, StandardCharsets.UTF_8);
        } else if (category != null) {
            products = catalog.findProductsByCategorySlug(category);
            req.setAttribute("activeCategory", category);
            currentUrl = "/?category=" + URLEncoder.encode(category, StandardCharsets.UTF_8);
        } else {
            products = catalog.getAllProducts();
            currentUrl = "/";
        }

        String sort = req.getParameter("sort");
        applySort(products, sort);

        Cart cart = WebUtils.getCart(req);

        req.setAttribute("sort", sort == null ? "" : sort);
        req.setAttribute("products", products);
        req.setAttribute("images", Images.forProducts(getServletContext(), products));
        req.setAttribute("productCount", Plural.ru(products.size(), "товар", "товара", "товаров"));
        req.setAttribute("categories", catalog.getCategories());
        req.setAttribute("cartCount", cart.getTotalCount());
        req.setAttribute("currentUrl", currentUrl);
        req.setAttribute("cur", WebUtils.getCurrency(req));
        req.setAttribute("pagePathEnc", WebUtils.urlEncode(currentUrl));

        // Показать одноразовые flash-сообщения (товар добавлен / заказ оформлен)
        WebUtils.consumeFlash(req);

        req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
    }

    /** Сортировка списка товаров по выбранному критерию. */
    private static void applySort(List<Product> products, String sort) {
        if (sort == null) {
            return;
        }
        switch (sort) {
            case "price_asc" -> products.sort(Comparator.comparing(Product::getPrice));
            case "price_desc" -> products.sort(Comparator.comparing(Product::getPrice).reversed());
            case "name" -> products.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
            default -> { /* relevance / default — порядок не меняем */ }
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
