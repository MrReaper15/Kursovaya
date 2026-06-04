<%@ tag body-content="empty" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="value" required="true" type="java.math.BigDecimal" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<span class="money">${requestScope.cur.format(value)}<c:choose><c:when test="${requestScope.cur.code eq 'BYN'}"> <svg class="cur-sign" aria-hidden="true"><use href="#cur-byn"></use></svg></c:when><c:otherwise> ₽</c:otherwise></c:choose></span>
