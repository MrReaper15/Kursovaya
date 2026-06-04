package com.buildingstore.util;

import com.buildingstore.model.Product;
import jakarta.servlet.ServletContext;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Подбор картинки для товара.
 *
 * Приоритет:
 *   1) реальное фото товара  — /img/photos/{id}.{jpg|jpeg|png|webp}
 *   2) реальное фото категории — /img/photos/{slug}.{jpg|jpeg|png|webp}
 *   3) векторная иллюстрация категории — /img/{slug}.svg  (есть всегда)
 *
 * То есть достаточно положить файл в src/main/webapp/img/photos/ —
 * и он автоматически появится в каталоге, без изменения кода.
 */
public final class Images {

    private Images() {}

    private static final String[] EXTENSIONS = {"jpg", "jpeg", "png", "webp"};
    private static final String PHOTOS_DIR = "/img/photos/";

    /** Имена файлов в папке /img/photos (в нижнем регистре). */
    private static Set<String> listPhotos(ServletContext ctx) {
        Set<String> names = new HashSet<>();
        Set<String> paths = ctx.getResourcePaths(PHOTOS_DIR);
        if (paths != null) {
            for (String path : paths) {
                String name = path.substring(path.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    private static String pick(Set<String> photos, Product product) {
        Long id = product.getId();
        String slug = product.getCategory() != null ? product.getCategory().getSlug() : null;

        for (String ext : EXTENSIONS) {
            String name = id + "." + ext;
            if (photos.contains(name)) {
                return PHOTOS_DIR + name;
            }
        }
        if (slug != null) {
            for (String ext : EXTENSIONS) {
                String name = slug + "." + ext;
                if (photos.contains(name)) {
                    return PHOTOS_DIR + name;
                }
            }
        }
        return "/img/" + (slug != null ? slug : "all") + ".svg";
    }

    /** Картинка для одного товара (относительный путь без contextPath). */
    public static String forProduct(ServletContext ctx, Product product) {
        return pick(listPhotos(ctx), product);
    }

    /** Картинки для списка товаров: id товара -> путь. */
    public static Map<Long, String> forProducts(ServletContext ctx, List<Product> products) {
        Set<String> photos = listPhotos(ctx);
        Map<Long, String> result = new LinkedHashMap<>();
        for (Product product : products) {
            result.put(product.getId(), pick(photos, product));
        }
        return result;
    }
}
