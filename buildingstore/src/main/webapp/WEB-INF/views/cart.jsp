<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <meta name="description" content="Корзина — СтройМаркет"/>
  <title>Корзина — СтройМаркет</title>
  <link rel="stylesheet" href="${ctx}/css/style.css"/>
</head>
<body>
<%@ include file="sprite.jspf" %>

<header class="nav">
  <div class="nav__inner">
    <a href="${ctx}/" class="brand"><img class="brand__logo" src="${ctx}/img/logo.svg" alt=""/>СтройМаркет</a>
    <form class="search" action="${ctx}/" method="get" role="search" autocomplete="off">
      <input type="text" name="search" id="search-input" placeholder="Поиск по каталогу" autocomplete="off"
             data-suggest="${ctx}/api/suggest" data-ctx="${ctx}"/>
      <div id="search-suggest" class="suggest"></div>
    </form>
    <nav class="nav__links">
      <div class="cur-switch">
        <a href="${ctx}/currency?c=RUB&back=${pagePathEnc}" title="Российский рубль"
           class="cur-switch__btn ${cur.code eq 'RUB' ? 'cur-switch__btn--active' : ''}">₽</a>
        <a href="${ctx}/currency?c=BYN&back=${pagePathEnc}" title="Белорусский рубль"
           class="cur-switch__btn ${cur.code eq 'BYN' ? 'cur-switch__btn--active' : ''}"><svg class="cur-sign"><use href="#cur-byn"></use></svg></a>
      </div>
      <a href="${ctx}/" class="nav__link">Каталог</a>
      <a href="${ctx}/cart" class="nav__link nav__link--active">Корзина<c:if test="${cartCount > 0}"><span class="cart-count">${cartCount}</span></c:if></a>
    </nav>
  </div>
</header>

<main class="cart">
  <h1 class="cart__title">Корзина</h1>

  <c:choose>
    <c:when test="${empty cartItems}">
      <div class="empty">
        <p class="empty__title">Корзина пуста</p>
        <p>Добавьте товары из каталога, чтобы оформить заказ.</p>
        <a href="${ctx}/" class="btn btn--primary">Перейти в каталог</a>
      </div>
    </c:when>
    <c:otherwise>

      <div class="cart-list">
        <c:forEach var="item" items="${cartItems}">
          <div class="cart-item">
            <div>
              <a href="${ctx}/product/${item.product.id}" class="cart-item__name"><c:out value="${item.product.name}"/></a>
              <div class="cart-item__cat"><c:out value="${item.product.category.name}"/></div>
            </div>

            <div class="stepper">
              <form action="${ctx}/cart/update/${item.product.id}" method="post">
                <input type="hidden" name="quantity" value="${item.quantity - 1}"/>
                <button type="submit" aria-label="Уменьшить">−</button>
              </form>
              <form action="${ctx}/cart/update/${item.product.id}" method="post">
                <input type="number" name="quantity" value="${item.quantity}" min="1"
                       onchange="this.form.submit()" aria-label="Количество"/>
              </form>
              <form action="${ctx}/cart/update/${item.product.id}" method="post">
                <input type="hidden" name="quantity" value="${item.quantity + 1}"/>
                <button type="submit" aria-label="Увеличить">+</button>
              </form>
            </div>

            <div class="cart-item__sum"><t:price value="${item.subtotal}"/></div>

            <form action="${ctx}/cart/remove/${item.product.id}" method="post">
              <button type="submit" class="icon-btn" title="Удалить" aria-label="Удалить">×</button>
            </form>
          </div>
        </c:forEach>
      </div>

      <div class="cart-actions">
        <a href="${ctx}/" class="btn btn--ghost">← Продолжить покупки</a>
        <form action="${ctx}/cart/clear" method="post">
          <button type="submit" class="btn btn--danger">Очистить корзину</button>
        </form>
      </div>

      <div class="summary">
        <h2>Итого</h2>
        <div class="summary__row"><span>Товаров</span><span>${cartCount} шт.</span></div>
        <div class="summary__row"><span>Доставка</span><span class="free">бесплатно</span></div>
        <div class="summary__total"><span>К оплате</span><span><t:price value="${cart.totalPrice}"/></span></div>

        <div class="checkout">
          <h3>Оформление заказа</h3>
          <form action="${ctx}/cart/checkout" method="post">
            <div class="field">
              <label for="name">Имя</label>
              <input type="text" name="name" id="name" placeholder="Иван Иванов" required/>
            </div>
            <div class="field">
              <label for="phone">Телефон</label>
              <input type="tel" name="phone" id="phone" placeholder="+7 999 000-00-00" required/>
            </div>
            <div class="field">
              <label for="address">Адрес доставки</label>
              <input type="text" name="address" id="address" placeholder="Город, улица, дом" required/>
            </div>
            <button type="submit" class="btn btn--primary btn--block">Оформить заказ</button>
          </form>
        </div>
      </div>

    </c:otherwise>
  </c:choose>
</main>

<footer class="footer">
  <div class="footer__inner">
    <span>© 2026 СтройМаркет</span>
    <span>8 800 555-35-35 · Москва, ул. Строителей, 1</span>
  </div>
</footer>

<script src="${ctx}/js/app.js"></script>
</body>
</html>
