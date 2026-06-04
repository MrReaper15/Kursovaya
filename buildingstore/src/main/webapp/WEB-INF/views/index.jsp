<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <meta name="description" content="СтройМаркет — материалы для строительства и ремонта: цемент, кирпич, утеплители, краски, инструменты, сантехника."/>
  <title>СтройМаркет — материалы для стройки и ремонта</title>
  <link rel="stylesheet" href="${ctx}/css/style.css"/>
</head>
<body>
<%@ include file="sprite.jspf" %>

<header class="nav">
  <div class="nav__inner">
    <a href="${ctx}/" class="brand"><img class="brand__logo" src="${ctx}/img/logo.svg" alt=""/>СтройМаркет</a>
    <form class="search" action="${ctx}/" method="get" role="search" autocomplete="off">
      <input type="text" name="search" id="search-input" placeholder="Поиск по каталогу" autocomplete="off"
             data-suggest="${ctx}/api/suggest" data-ctx="${ctx}"
             value="<c:out value='${searchQuery}'/>"/>
      <div id="search-suggest" class="suggest"></div>
    </form>
    <nav class="nav__links">
      <div class="cur-switch">
        <a href="${ctx}/currency?c=RUB&back=${pagePathEnc}" title="Российский рубль"
           class="cur-switch__btn ${cur.code eq 'RUB' ? 'cur-switch__btn--active' : ''}">₽</a>
        <a href="${ctx}/currency?c=BYN&back=${pagePathEnc}" title="Белорусский рубль"
           class="cur-switch__btn ${cur.code eq 'BYN' ? 'cur-switch__btn--active' : ''}"><svg class="cur-sign"><use href="#cur-byn"></use></svg></a>
      </div>
      <a href="${ctx}/" class="nav__link ${empty activeCategory and empty searchQuery ? 'nav__link--active' : ''}">Каталог</a>
      <a href="${ctx}/cart" class="nav__link">Корзина<c:if test="${cartCount > 0}"><span class="cart-count">${cartCount}</span></c:if></a>
    </nav>
  </div>
</header>

<c:if test="${empty searchQuery and empty activeCategory}">
  <section class="intro">
    <div class="intro__inner">
      <h1>Материалы для стройки и&nbsp;ремонта</h1>
      <p>Цемент, кирпич, утеплители, краски, инструменты и сантехника. Со склада в Москве, с доставкой по городу.</p>
    </div>
  </section>
</c:if>

<main class="layout">
  <aside class="sidebar">
    <p class="cats__title">Категории</p>
    <ul class="cats">
      <li>
        <a href="${ctx}/" class="cats__link ${empty activeCategory and empty searchQuery ? 'cats__link--active' : ''}">
          <svg class="cats__icon"><use href="#icon-all"></use></svg>Все товары
        </a>
      </li>
      <c:forEach var="cat" items="${categories}">
        <li>
          <a href="${ctx}/?category=${cat.slug}" class="cats__link ${activeCategory == cat.slug ? 'cats__link--active' : ''}">
            <svg class="cats__icon"><use href="#icon-${cat.slug}"></use></svg><c:out value="${cat.name}"/>
          </a>
        </li>
      </c:forEach>
    </ul>
  </aside>

  <section class="content">
    <c:if test="${not empty searchQuery}">
      <p class="searchnote">
        Результаты по запросу «<c:out value="${searchQuery}"/>» · <a href="${ctx}/">сбросить</a>
      </p>
    </c:if>

    <div class="content__head">
      <h2>
        <c:choose>
          <c:when test="${not empty activeCategory}">
            <c:forEach var="cat" items="${categories}">
              <c:if test="${cat.slug == activeCategory}"><c:out value="${cat.name}"/></c:if>
            </c:forEach>
          </c:when>
          <c:when test="${not empty searchQuery}">Результаты поиска</c:when>
          <c:otherwise>Все товары</c:otherwise>
        </c:choose>
      </h2>
      <div class="content__tools">
        <span class="count">${productCount}</span>
        <form class="sort" action="${ctx}/" method="get">
          <c:if test="${not empty searchQuery}"><input type="hidden" name="search" value="<c:out value='${searchQuery}'/>"/></c:if>
          <c:if test="${not empty activeCategory}"><input type="hidden" name="category" value="${activeCategory}"/></c:if>
          <select name="sort" onchange="this.form.submit()" aria-label="Сортировка">
            <option value="">Сортировка</option>
            <option value="price_asc" ${sort eq 'price_asc' ? 'selected' : ''}>Сначала дешевле</option>
            <option value="price_desc" ${sort eq 'price_desc' ? 'selected' : ''}>Сначала дороже</option>
            <option value="name" ${sort eq 'name' ? 'selected' : ''}>По названию</option>
          </select>
        </form>
      </div>
    </div>

    <c:choose>
      <c:when test="${not empty products}">
        <div class="grid">
          <c:forEach var="product" items="${products}">
            <article class="card">
              <div class="thumb">
                <c:set var="img" value="${images[product.id]}"/>
                <img class="thumb__img ${img.contains('/photos/') ? 'thumb__img--photo' : ''}"
                     src="${ctx}${img}" alt="<c:out value='${product.name}'/>" loading="lazy"/>
                <span class="thumb__cat"><c:out value="${product.category.name}"/></span>
              </div>
              <div class="card__body">
                <h3 class="card__name"><c:out value="${product.name}"/></h3>
                <p class="card__desc"><c:out value="${product.description}"/></p>
                <div class="card__foot">
                  <div class="price"><t:price value="${product.price}"/><span class="price__unit">за <c:out value="${product.unit}"/></span></div>
                  <form action="${ctx}/cart/add/${product.id}" method="post">
                    <input type="hidden" name="quantity" value="1"/>
                    <input type="hidden" name="redirect" value="<c:out value='${currentUrl}'/>"/>
                    <button type="submit" class="btn btn--primary btn--sm">В корзину</button>
                  </form>
                </div>
              </div>
              <a href="${ctx}/product/${product.id}" class="card__link">Подробнее</a>
            </article>
          </c:forEach>
        </div>
      </c:when>
      <c:otherwise>
        <div class="empty">
          <p class="empty__title">Ничего не найдено</p>
          <p>Попробуйте изменить запрос или выбрать другую категорию.</p>
          <a href="${ctx}/" class="btn btn--ghost">Показать все товары</a>
        </div>
      </c:otherwise>
    </c:choose>
  </section>
</main>

<c:if test="${not empty message}">
  <div class="flash"><c:out value="${message}"/></div>
</c:if>
<c:if test="${orderSuccess}">
  <div class="flash flash--order">Заказ оформлен. Спасибо, <c:out value="${clientName}"/>!</div>
</c:if>

<footer class="footer">
  <div class="footer__inner">
    <span>© 2026 СтройМаркет</span>
    <span>8 800 555-35-35 · Москва, ул. Строителей, 1</span>
  </div>
</footer>

<script>
  document.querySelectorAll('.flash').forEach(function (el) {
    setTimeout(function () { el.remove(); }, 3600);
  });
</script>
<script src="${ctx}/js/app.js"></script>
</body>
</html>
