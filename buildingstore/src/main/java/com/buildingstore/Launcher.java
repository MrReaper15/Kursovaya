package com.buildingstore;

import com.buildingstore.servlet.CacheFilter;
import com.buildingstore.servlet.CartServlet;
import com.buildingstore.servlet.CurrencyServlet;
import com.buildingstore.servlet.HomeServlet;
import com.buildingstore.servlet.ProductServlet;
import com.buildingstore.servlet.SuggestServlet;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.io.File;

/**
 * Точка входа приложения. Поднимает встроенный сервер Apache Tomcat —
 * отдельно устанавливать и настраивать сервер не нужно.
 *
 * Запуск:
 *   • в Eclipse / IDE — Run As → Java Application (этот класс);
 *   • из терминала    — ./mvnw compile exec:java  (или mvn compile exec:java).
 *
 * После старта откройте: http://localhost:8080
 */
public class Launcher {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws LifecycleException {
        int port = resolvePort();

        // Каталог с веб-содержимым (JSP, CSS, картинки)
        File webappDir = resolveWebappDir();
        if (!webappDir.isDirectory()) {
            System.err.println("[ОШИБКА] Не найден каталог веб-приложения: " + webappDir.getAbsolutePath());
            System.err.println("Запускайте приложение из корневой папки проекта (где лежит pom.xml).");
            return;
        }

        // Рабочий каталог Tomcat (компиляция JSP и временные файлы) — во временной папке системы
        File baseDir = new File(System.getProperty("java.io.tmpdir"), "buildingstore-tomcat");

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.getAbsolutePath());
        tomcat.setPort(port);

        // HTTP-коннектор + gzip-сжатие текстовых ответов (HTML, CSS, JS, JSON, SVG)
        Connector connector = tomcat.getConnector();
        connector.setProperty("compression", "on");
        connector.setProperty("compressionMinSize", "1024");
        connector.setProperty("compressibleMimeType",
                "text/html,text/css,text/plain,text/javascript,application/javascript,application/json,image/svg+xml");
        connector.setProperty("URIEncoding", "UTF-8");

        Context ctx = tomcat.addWebapp("", webappDir.getAbsolutePath());

        // Кодировка UTF-8 по умолчанию для тела запросов (POST-формы) и ответов —
        // иначе кириллица из форм (имя, адрес) приходит «крякозябрами».
        ctx.setRequestCharacterEncoding("UTF-8");
        ctx.setResponseCharacterEncoding("UTF-8");

        // Гарантируем корректный MIME-тип для SVG-картинок товаров и логотипа.
        ctx.addMimeMapping("svg", "image/svg+xml");

        // Кэширование статики браузером (картинки, стили, скрипты) — на 1 день.
        FilterDef cacheFilter = new FilterDef();
        cacheFilter.setFilterName("cache");
        cacheFilter.setFilter(new CacheFilter(86400));
        ctx.addFilterDef(cacheFilter);
        FilterMap cacheMap = new FilterMap();
        cacheMap.setFilterName("cache");
        cacheMap.addURLPattern("/img/*");
        cacheMap.addURLPattern("/css/*");
        cacheMap.addURLPattern("/js/*");
        ctx.addFilterMap(cacheMap);

        // Регистрируем сервлеты программно — самый надёжный способ для встроенного Tomcat
        Tomcat.addServlet(ctx, "home", new HomeServlet());
        ctx.addServletMappingDecoded("", "home");            // корень сайта "/"

        Tomcat.addServlet(ctx, "product", new ProductServlet());
        ctx.addServletMappingDecoded("/product/*", "product");

        Tomcat.addServlet(ctx, "cart", new CartServlet());
        ctx.addServletMappingDecoded("/cart/*", "cart");

        Tomcat.addServlet(ctx, "currency", new CurrencyServlet());
        ctx.addServletMappingDecoded("/currency", "currency");

        Tomcat.addServlet(ctx, "suggest", new SuggestServlet());
        ctx.addServletMappingDecoded("/api/suggest", "suggest");

        tomcat.start();

        System.out.println();
        System.out.println("======================================");
        System.out.println("  СтройМаркет запущен!");
        System.out.println("  Откройте в браузере: http://localhost:" + port);
        System.out.println("  Остановка сервера: Ctrl+C");
        System.out.println("======================================");
        System.out.println();

        tomcat.getServer().await(); // блокируемся, пока сервер работает
    }

    private static int resolvePort() {
        String env = System.getenv("PORT");
        if (env != null) {
            try {
                return Integer.parseInt(env.trim());
            } catch (NumberFormatException ignored) {
                // используем порт по умолчанию
            }
        }
        return DEFAULT_PORT;
    }

    /** Ищет каталог src/main/webapp относительно текущего рабочего каталога. */
    private static File resolveWebappDir() {
        File candidate = new File("src/main/webapp");
        if (candidate.isDirectory()) {
            return candidate.getAbsoluteFile();
        }
        return new File(System.getProperty("user.dir"), "src/main/webapp").getAbsoluteFile();
    }
}
