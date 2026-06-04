package com.buildingstore.servlet;

import com.buildingstore.data.Catalog;
import com.buildingstore.model.Cart;
import com.buildingstore.model.Product;
import com.buildingstore.util.Images;
import com.buildingstore.util.WebUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * Страница товара. Адрес вида «/product/{id}».
 */
public class ProductServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final Catalog catalog = Catalog.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Long id = extractId(req.getPathInfo());
        Optional<Product> product = catalog.findProductById(id);

        if (product.isEmpty()) {
            // Товар не найден — возвращаемся в каталог
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        Cart cart = WebUtils.getCart(req);

        req.setAttribute("product", product.get());
        req.setAttribute("productImage", Images.forProduct(getServletContext(), product.get()));
        req.setAttribute("categories", catalog.getCategories());
        req.setAttribute("cartCount", cart.getTotalCount());
        req.setAttribute("currentUrl", "/product/" + product.get().getId());
        req.setAttribute("cur", WebUtils.getCurrency(req));
        req.setAttribute("pagePathEnc", WebUtils.urlEncode("/product/" + product.get().getId()));

        WebUtils.consumeFlash(req);

        req.getRequestDispatcher("/WEB-INF/views/product.jsp").forward(req, resp);
    }

    /** Извлекает id из pathInfo вида «/5». */
    private static Long extractId(String pathInfo) {
        if (pathInfo == null) {
            return null;
        }
        String raw = pathInfo;
        if (raw.startsWith("/")) {
            raw = raw.substring(1);
        }
        int slash = raw.indexOf('/');
        if (slash >= 0) {
            raw = raw.substring(0, slash);
        }
        return WebUtils.parseLong(raw);
    }
}
