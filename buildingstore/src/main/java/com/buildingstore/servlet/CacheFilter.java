package com.buildingstore.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Простое кэширование статики браузером: добавляет заголовок Cache-Control
 * для картинок, стилей и скриптов, чтобы при повторных заходах не качать их снова.
 */
public class CacheFilter implements Filter {

    private final long maxAgeSeconds;

    public CacheFilter(long maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse http) {
            http.setHeader("Cache-Control", "public, max-age=" + maxAgeSeconds);
        }
        chain.doFilter(request, response);
    }
}
