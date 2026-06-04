package com.buildingstore.data;

import com.buildingstore.model.Category;
import com.buildingstore.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Каталог товаров и категорий. Хранится в оперативной памяти (in-memory),
 * без базы данных — это самый надёжный вариант для прототипа.
 *
 * Реализован как потокобезопасный синглтон: данные заполняются один раз
 * при старте приложения и далее только читаются.
 */
public final class Catalog {

    private static final Catalog INSTANCE = new Catalog();

    public static Catalog getInstance() {
        return INSTANCE;
    }

    private final List<Category> categories = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();

    private long categorySeq = 0;
    private long productSeq = 0;

    private Catalog() {
        seed();
    }

    // ----------------------- ПУБЛИЧНЫЕ ЗАПРОСЫ -----------------------

    public List<Category> getCategories() {
        return new ArrayList<>(categories);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public Optional<Product> findProductById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return products.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public Optional<Category> findCategoryBySlug(String slug) {
        if (slug == null) {
            return Optional.empty();
        }
        return categories.stream().filter(c -> c.getSlug().equalsIgnoreCase(slug)).findFirst();
    }

    public List<Product> findProductsByCategorySlug(String slug) {
        Optional<Category> category = findCategoryBySlug(slug);
        if (category.isEmpty()) {
            return getAllProducts();
        }
        Long categoryId = category.get().getId();
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.getCategory() != null && p.getCategory().getId().equals(categoryId)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Умный поиск: разбивает запрос на слова, ищет по названию, категории,
     * описанию и единице измерения, нормализует регистр и «ё»→«е», и
     * сортирует результаты по релевантности (совпадение в названии важнее).
     * Товар попадает в выдачу, только если найдены ВСЕ слова запроса.
     */
    public List<Product> search(String query) {
        if (query == null || query.isBlank()) {
            return getAllProducts();
        }
        String normalizedQuery = normalize(query);
        String[] tokens = normalizedQuery.split("\\s+");

        List<Product> matched = new ArrayList<>();
        Map<Long, Integer> scores = new HashMap<>();

        for (Product p : products) {
            String name = normalize(safe(p.getName()));
            String category = normalize(p.getCategory() != null ? safe(p.getCategory().getName()) : "");
            String description = normalize(safe(p.getDescription()));
            String unit = normalize(safe(p.getUnit()));

            int total = 0;
            boolean allTokensFound = true;
            for (String token : tokens) {
                if (token.isEmpty()) {
                    continue;
                }
                int tokenScore = 0;
                if (name.contains(token)) {
                    tokenScore += 10;
                    if (name.startsWith(token) || name.contains(" " + token)) {
                        tokenScore += 5; // совпадение в начале слова
                    }
                }
                if (category.contains(token)) {
                    tokenScore += 4;
                }
                if (description.contains(token)) {
                    tokenScore += 2;
                }
                if (unit.contains(token)) {
                    tokenScore += 1;
                }
                if (tokenScore == 0) {
                    allTokensFound = false;
                    break;
                }
                total += tokenScore;
            }

            if (allTokensFound && total > 0) {
                if (name.contains(normalizedQuery)) {
                    total += 8; // вся фраза целиком в названии
                }
                matched.add(p);
                scores.put(p.getId(), total);
            }
        }

        matched.sort(Comparator
                .comparingInt((Product p) -> scores.getOrDefault(p.getId(), 0)).reversed()
                .thenComparing(p -> safe(p.getName()), String.CASE_INSENSITIVE_ORDER));
        return matched;
    }

    /** Регистр в нижний, «ё»→«е», обрезка пробелов. */
    private static String normalize(String s) {
        return s.toLowerCase(Locale.ROOT).replace('ё', 'е').trim();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    // ----------------------- ЗАПОЛНЕНИЕ ДАННЫХ -----------------------

    private Category addCategory(String name, String slug) {
        Category c = new Category(++categorySeq, name, slug);
        categories.add(c);
        return c;
    }

    private void addProduct(String name, String description, String price,
                            int stock, String unit, Category category) {
        products.add(new Product(++productSeq, name, description,
                new BigDecimal(price), stock, unit, category));
    }

    /** Заполнение каталога. Цены актуализированы на 2026 год. */
    private void seed() {
        Category cement     = addCategory("Цемент и смеси",     "cement");
        Category brick      = addCategory("Кирпич и блоки",     "brick");
        Category insulation = addCategory("Утеплители",         "insulation");
        Category paint      = addCategory("Краски и лаки",      "paint");
        Category tools      = addCategory("Инструменты",        "tools");
        Category flooring   = addCategory("Напольные покрытия", "flooring");
        Category roofing    = addCategory("Кровля",             "roofing");
        Category plumbing   = addCategory("Сантехника",         "plumbing");

        // === Цемент и смеси ===
        addProduct("Цемент ПЦ 500 Д0, 50 кг",
                "Портландцемент высокой марки для несущих конструкций, фундаментов и стяжек. Прочность на сжатие 500 кг/см².",
                "520.00", 200, "мешок", cement);
        addProduct("Цемент ПЦ 400 Д20, 50 кг",
                "Универсальный цемент для общестроительных работ. Подходит для кладки, штукатурки и стяжек.",
                "460.00", 300, "мешок", cement);
        addProduct("Пескобетон М300, 50 кг",
                "Сухая смесь для стяжек пола, оснований под тротуарную плитку, фундаментных работ.",
                "360.00", 400, "мешок", cement);
        addProduct("Штукатурка гипсовая Ротбанд, 30 кг",
                "Профессиональная штукатурка Knauf для выравнивания стен и потолков. Белоснежный финиш.",
                "790.00", 150, "мешок", cement);
        addProduct("Клей плиточный Ceresit CM11, 25 кг",
                "Универсальный цементный клей для керамической плитки в ванных, кухнях и на полу.",
                "590.00", 250, "мешок", cement);

        // === Кирпич и блоки ===
        addProduct("Кирпич рядовой полнотелый М150",
                "Одинарный строительный кирпич 250×120×65 мм. Подходит для кладки несущих стен и перегородок.",
                "24.50", 10000, "шт", brick);
        addProduct("Кирпич облицовочный красный М200",
                "Декоративный кирпич с ровной поверхностью для отделки фасадов и заборов. М200.",
                "42.00", 5000, "шт", brick);
        addProduct("Газобетонный блок D500, 600×300×200",
                "Лёгкий ячеистый бетон для строительства стен. Отличная теплоизоляция, лёгкий монтаж.",
                "185.00", 2000, "шт", brick);
        addProduct("Блок пенобетонный 600×300×200 мм",
                "Пористые блоки D600 для внутренних перегородок и наружных стен малоэтажных зданий.",
                "145.00", 3000, "шт", brick);

        // === Утеплители ===
        addProduct("Пенопласт ПСБ-С 25, 50 мм (1.2×0.6 м)",
                "Листы пенополистирола для утепления фасадов, стен, полов и кровли. Плотность 25 кг/м³.",
                "245.00", 500, "лист", insulation);
        addProduct("Минвата Rockwool Лайт Баттс, 50 мм",
                "Рулонная каменная вата для утепления кровли, перекрытий и каркасных стен. Негорючая.",
                "2350.00", 80, "упак", insulation);
        addProduct("Пеноплекс Comfort 50 мм (1.185×0.585 м)",
                "Экструдированный пенополистирол для утепления фундаментов, полов и фасадов. Влагостойкий.",
                "410.00", 300, "лист", insulation);
        addProduct("Стекловата ISOVER 50 мм, рулон 15 м²",
                "Лёгкая стекловата для межкомнатных перегородок и вентилируемых фасадов.",
                "1790.00", 60, "рулон", insulation);

        // === Краски и лаки ===
        addProduct("Краска акриловая белая интерьерная, 10 л",
                "Матовая белая краска для стен и потолков внутри помещений. Быстросохнущая, без запаха.",
                "1590.00", 100, "ведро", paint);
        addProduct("Краска фасадная Caparol, 10 л",
                "Атмосферостойкая краска для наружной отделки. Защищает от влаги, УФ и грибка.",
                "3500.00", 50, "ведро", paint);
        addProduct("Эмаль алкидная ПФ-115 белая, 3 кг",
                "Глянцевая эмаль для металла и дерева. Применяется на радиаторах, трубах, окнах.",
                "650.00", 200, "банка", paint);
        addProduct("Лак паркетный яхтный, 2.7 л",
                "Полиуретановый лак для деревянных полов и мебели. Износостойкий, водонепроницаемый.",
                "1240.00", 80, "банка", paint);
        addProduct("Грунтовка Ceresit CT17 глубокого проникновения, 10 л",
                "Укрепляет основание, снижает впитываемость перед нанесением штукатурки или краски.",
                "890.00", 150, "ведро", paint);

        // === Инструменты ===
        addProduct("Перфоратор Bosch GBH 2-26 DRE",
                "Профессиональный перфоратор 800 Вт. SDS-plus, три режима: сверление, удар, долбление.",
                "15900.00", 15, "шт", tools);
        addProduct("Шуруповёрт DeWalt DCD796",
                "Аккумуляторный шуруповёрт 18V с бесщёточным двигателем. Комплект с двумя АКБ 2 Ач.",
                "23900.00", 10, "шт", tools);
        addProduct("Болгарка Makita GA9020, 230 мм",
                "Угловая шлифмашина 2000 Вт для резки металла, бетона и камня. Диск 230 мм.",
                "10700.00", 12, "шт", tools);
        addProduct("Уровень пузырьковый 120 см",
                "Алюминиевый строительный уровень с тремя ампулами. Точность ±0.5 мм/м.",
                "1090.00", 40, "шт", tools);
        addProduct("Мастерок штукатурный 200 мм",
                "Нержавеющий мастерок с деревянной рукоятью для штукатурных и кладочных работ.",
                "410.00", 100, "шт", tools);

        // === Напольные покрытия ===
        addProduct("Ламинат Egger 33 класс, дуб натуральный, 8 мм",
                "Влагостойкий ламинат с фаской. Замковое соединение Click. Площадь в упаковке 2.42 м².",
                "2090.00", 200, "упак", flooring);
        addProduct("Плитка керамогранит 60×60 см, серый",
                "Матовая напольная плитка под бетон. Ректифицированная, морозостойкая. 1.44 м² в упак.",
                "2490.00", 300, "упак", flooring);
        addProduct("Линолеум бытовой 3 м, серый мрамор",
                "Износостойкий линолеум на тёплой подложке. Толщина 3 мм. Продаётся за 1 п.м.",
                "580.00", 500, "п.м.", flooring);

        // === Кровля ===
        addProduct("Металлочерепица МП Монтеррей 0.45 мм",
                "Оцинкованный лист с полимерным покрытием. Длина 1.185 м. Цвет: красно-коричневый.",
                "1150.00", 600, "лист", roofing);
        addProduct("Профнастил С8 оцинкованный, 2 м",
                "Волнистый оцинкованный лист 0.4 мм для кровли и заборов. Ширина 1.2 м.",
                "850.00", 400, "лист", roofing);
        addProduct("Гидроизоляция Технониколь Альфа, рулон 70 м²",
                "Диффузионная паропроницаемая мембрана для скатных кровель. Защита от влаги и ветра.",
                "2790.00", 50, "рулон", roofing);

        // === Сантехника ===
        addProduct("Радиатор биметаллический Royal Thermo, 8 секций",
                "Биметаллический радиатор 500/100 мм. Рабочее давление 20 атм. Мощность 1360 Вт.",
                "7300.00", 30, "шт", plumbing);
        addProduct("Труба полипропиленовая PN20 25 мм, 4 м",
                "Армированная PPR труба для горячей и холодной воды. Рабочая температура до 95°C.",
                "420.00", 200, "шт", plumbing);
        addProduct("Смеситель для ванны GROHE BauEdge",
                "Однорычажный смеситель хромированный. Картридж 35 мм, аэратор, гибкая подводка в комплекте.",
                "5300.00", 20, "шт", plumbing);

        System.out.println("Каталог загружен: категорий — " + categories.size()
                + ", товаров — " + products.size());
    }
}
