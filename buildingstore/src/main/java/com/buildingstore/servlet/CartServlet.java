package com.buildingstore.servlet;

import com.buildingstore.data.Catalog;
import com.buildingstore.model.Cart;
import com.buildingstore.model.Product;
import com.buildingstore.util.WebUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * Корзина. Один сервлет на адрес «/cart/*»:
 *   GET  /cart              — просмотр корзины
 *   POST /cart/add/{id}     — добавить товар
 *   POST /cart/update/{id}  — изменить количество
 *   POST /cart/remove/{id}  — удалить позицию
 *   POST /cart/clear        — очистить корзину
 *   POST /cart/checkout     — оформить заказ
 */
public class CartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final Catalog catalog = Catalog.getInstance();

    // ----------------------- ПРОСМОТР КОРЗИНЫ -----------------------

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Cart cart = WebUtils.getCart(req);

        req.setAttribute("cart", cart);
        req.setAttribute("cartItems", cart.getItems());
        req.setAttribute("cartCount", cart.getTotalCount());
        req.setAttribute("categories", catalog.getCategories());
        req.setAttribute("cur", WebUtils.getCurrency(req));
        req.setAttribute("pagePathEnc", WebUtils.urlEncode("/cart"));

        WebUtils.consumeFlash(req);

        req.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(req, resp);
    }

    // ----------------------- ДЕЙСТВИЯ С КОРЗИНОЙ -----------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Гарантируем чтение тела POST-формы в UTF-8 (кириллица в имени/адресе)
        req.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(req.getPathInfo());
        String action = parts.length > 0 ? parts[0] : "";
        Long id = parts.length > 1 ? WebUtils.parseLong(parts[1]) : null;

        Cart cart = WebUtils.getCart(req);
        String context = req.getContextPath();

        switch (action) {
            case "add" -> {
                Optional<Product> product = catalog.findProductById(id);
                if (product.isPresent()) {
                    int quantity = WebUtils.parseInt(req.getParameter("quantity"), 1);
                    if (quantity < 1) {
                        quantity = 1;
                    }
                    cart.add(product.get(), quantity);
                    WebUtils.setFlashMessage(req, "Товар добавлен в корзину!");
                }
                String redirect = WebUtils.safeLocalRedirect(req.getParameter("redirect"));
                resp.sendRedirect(context + redirect);
            }
            case "update" -> {
                int quantity = WebUtils.parseInt(req.getParameter("quantity"), 1);
                cart.update(id, quantity);
                resp.sendRedirect(context + "/cart");
            }
            case "remove" -> {
                cart.remove(id);
                resp.sendRedirect(context + "/cart");
            }
            case "clear" -> {
                cart.clear();
                resp.sendRedirect(context + "/cart");
            }
            case "checkout" -> {
                String name = trim(req.getParameter("name"));
                String phone = trim(req.getParameter("phone"));
                String address = trim(req.getParameter("address"));

                // Заказ можно оформить только если корзина не пуста и заполнены поля
                if (cart.isEmpty() || name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                    resp.sendRedirect(context + "/cart");
                    return;
                }
                cart.clear();
                WebUtils.setFlashOrder(req, name);
                resp.sendRedirect(context + "/");
            }
            default -> resp.sendRedirect(context + "/cart");
        }
    }

    /** Разбивает pathInfo вида «/add/5» на ["add", "5"]. */
    private static String[] splitPath(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank() || pathInfo.equals("/")) {
            return new String[0];
        }
        String trimmed = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        return trimmed.split("/");
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
