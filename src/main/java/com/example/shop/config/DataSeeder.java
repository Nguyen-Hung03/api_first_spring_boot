package com.example.shop.config;

import com.example.shop.api.model.CustomerRequestDto;
import com.example.shop.api.model.OrderCreateRequestDto;
import com.example.shop.api.model.OrderItemRequestDto;
import com.example.shop.api.model.OrderStatusDto;
import com.example.shop.api.model.ProductRequestDto;
import com.example.shop.entity.Customer;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.service.CustomerService;
import com.example.shop.service.OrderService;
import com.example.shop.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Inserts demo data when the database is still the small starter set, so lists and pagination have something to show. */
@Component
@ConditionalOnProperty(name = "shop.seed-data", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderService orderService;

    public DataSeeder(CustomerRepository customerRepository, ProductRepository productRepository,
                      OrderRepository orderRepository, CustomerService customerService,
                      ProductService productService, OrderService orderService) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, Long> customers = seedCustomers();
        Map<String, Long> products = seedProducts();
        int orders = seedOrders(customers, products);
        log.info("Demo data ready: {} customers, {} products, {} new orders this run",
                customerRepository.count(), productRepository.count(), orders);
    }

    private Map<String, Long> seedCustomers() {
        record Row(String name, String email, String phone, String address) {
        }
        List<Row> rows = List.of(
                new Row("Nguyễn Văn An", "an.nguyen@example.com", "0912345678", "8 Tôn Thất Thuyết, Cầu Giấy, Hà Nội"),
                new Row("Trần Thị Bình", "binh.tran@example.com", "0987654321", "1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội"),
                new Row("Lê Minh Châu", "chau.le@example.com", "0901234567", "12 Nguyễn Huệ, Quận 1, TP.HCM"),
                new Row("Phạm Thu Hà", "ha.pham@example.com", "0911222333", "20 Bạch Đằng, Hải Châu, Đà Nẵng"),
                new Row("Đỗ Quang Minh", "minh.do@example.com", "0933444555", "5 Lạch Tray, Ngô Quyền, Hải Phòng"),
                new Row("Vũ Ngọc Lan", "lan.vu@example.com", "0977888999", "45 Hòa Bình, Ninh Kiều, Cần Thơ"),
                new Row("Hoàng Anh Tú", "tu.hoang@example.com", "0909090909", "18 Lê Lợi, Thành phố Huế"),
                new Row("Ngô Thanh Tâm", "tam.ngo@example.com", "0966123789", "9 Trần Phú, Nha Trang"),
                new Row("Bùi Đức Long", "long.bui@example.com", "0918654321", "102 Giải Phóng, Hai Bà Trưng, Hà Nội"),
                new Row("Đặng Mai Linh", "linh.dang@example.com", "0988123456", "27 Lê Duẩn, Quận 1, TP.HCM"),
                new Row("Trịnh Quốc Bảo", "bao.trinh@example.com", "0903555777", "6 Trần Hưng Đạo, Đà Lạt"),
                new Row("Lý Hồng Nhung", "nhung.ly@example.com", "0944666888", "15 Nguyễn Lương Bằng, Hải Dương")
        );
        Map<String, Long> ids = new LinkedHashMap<>();
        for (Row row : rows) {
            Long id = customerRepository.findByEmailIgnoreCase(row.email())
                    .map(Customer::getId)
                    .orElseGet(() -> customerService.create(
                            new CustomerRequestDto(row.name(), row.email()).phone(row.phone()).address(row.address())).getId());
            ids.put(row.email(), id);
        }
        return ids;
    }

    private Map<String, Long> seedProducts() {
        record Row(String name, String description, String category, String price, int stock) {
        }
        List<Row> rows = List.of(
                new Row("iPhone 16", "Apple iPhone 16 128GB", "Smartphone", "22990000", 50),
                new Row("Samsung Galaxy S25", "Samsung Galaxy S25 256GB", "Smartphone", "19990000", 40),
                new Row("MacBook Air M3", "MacBook Air 13 inch M3 8GB/256GB", "Laptop", "26490000", 20),
                new Row("Dell XPS 14", "Dell XPS 14 Ultra 7 16GB/512GB", "Laptop", "32990000", 12),
                new Row("iPad Air", "iPad Air 11 inch M2 128GB", "Tablet", "16990000", 25),
                new Row("Kindle Paperwhite", "Máy đọc sách Amazon Kindle Paperwhite", "Tablet", "3990000", 40),
                new Row("AirPods Pro 2", "Tai nghe chống ồn Apple AirPods Pro 2", "Âm thanh", "5990000", 100),
                new Row("Sony WH-1000XM5", "Tai nghe chống ồn Sony WH-1000XM5", "Âm thanh", "8490000", 30),
                new Row("Apple Watch SE", "Apple Watch SE GPS 40mm", "Đồng hồ", "6990000", 18),
                new Row("Logitech MX Master 3S", "Chuột không dây Logitech MX Master 3S", "Phụ kiện", "2490000", 8),
                new Row("Keychron K2", "Bàn phím cơ Keychron K2", "Phụ kiện", "2190000", 15),
                new Row("Sạc Apple 20W", "Củ sạc USB-C Apple 20W", "Phụ kiện", "549000", 0),
                new Row("Anker PowerCore", "Pin sạc dự phòng Anker 20000mAh", "Phụ kiện", "890000", 4),
                new Row("LG UltraFine 27", "Màn hình LG UltraFine 27 inch 4K", "Màn hình", "12490000", 6),
                new Row("ASUS ROG Ally", "Máy chơi game cầm tay ASUS ROG Ally", "Gaming", "15990000", 2)
        );
        Map<String, Long> ids = productRepository.findAll().stream()
                .collect(LinkedHashMap::new, (map, product) -> map.putIfAbsent(product.getName(), product.getId()), Map::putAll);
        for (Row row : rows) {
            ids.computeIfAbsent(row.name(), name -> productService.create(new ProductRequestDto(name, new BigDecimal(row.price()), row.stock())
                    .description(row.description())
                    .category(row.category())).getId());
        }
        return ids;
    }

    private int seedOrders(Map<String, Long> customers, Map<String, Long> products) {
        if (orderRepository.count() > 1) {
            return 0;
        }
        boolean includeFirst = orderRepository.count() == 0;
        int created = 0;
        if (includeFirst) {
            place(customers.get("an.nguyen@example.com"), "Giao giờ hành chính", products, "iPhone 16", 1, "MacBook Air M3", 1);
            created++;
        }
        created += placeAndAdvance(customers.get("binh.tran@example.com"), "Gọi trước khi giao",
                List.of(OrderStatusDto.CONFIRMED), products, "Samsung Galaxy S25", 1, "AirPods Pro 2", 1);
        created += placeAndAdvance(customers.get("chau.le@example.com"), null,
                List.of(OrderStatusDto.CONFIRMED, OrderStatusDto.SHIPPING), products, "Logitech MX Master 3S", 1);
        created += placeAndAdvance(customers.get("ha.pham@example.com"), "Đã nhận tại cửa hàng",
                List.of(OrderStatusDto.CONFIRMED, OrderStatusDto.SHIPPING, OrderStatusDto.DELIVERED),
                products, "iPad Air", 1, "Apple Watch SE", 1);
        created += placeAndAdvance(customers.get("minh.do@example.com"), "Khách đổi ý",
                List.of(OrderStatusDto.CANCELLED), products, "Keychron K2", 2);
        place(customers.get("lan.vu@example.com"), "Giao cuối tuần", products, "LG UltraFine 27", 1);
        created++;
        created += placeAndAdvance(customers.get("tu.hoang@example.com"), null,
                List.of(OrderStatusDto.CONFIRMED), products, "Sony WH-1000XM5", 1, "Anker PowerCore", 1);
        created += placeAndAdvance(customers.get("linh.dang@example.com"), "Để lễ tân nhận",
                List.of(OrderStatusDto.CONFIRMED, OrderStatusDto.SHIPPING), products, "Dell XPS 14", 1);
        place(customers.get("bao.trinh@example.com"), null, products, "Kindle Paperwhite", 2);
        created++;
        created += placeAndAdvance(customers.get("nhung.ly@example.com"), "Hủy vì đặt nhầm",
                List.of(OrderStatusDto.CANCELLED), products, "Samsung Galaxy S25", 1);
        return created;
    }

    private int placeAndAdvance(Long customerId, String note, List<OrderStatusDto> steps,
                                Map<String, Long> products, Object... nameAndQuantity) {
        long orderId = place(customerId, note, products, nameAndQuantity);
        for (OrderStatusDto step : steps) {
            orderService.updateStatus(orderId, step);
        }
        return 1;
    }

    private long place(Long customerId, String note, Map<String, Long> products, Object... nameAndQuantity) {
        List<OrderItemRequestDto> items = new java.util.ArrayList<>();
        for (int i = 0; i < nameAndQuantity.length; i += 2) {
            String name = (String) nameAndQuantity[i];
            int quantity = (Integer) nameAndQuantity[i + 1];
            items.add(new OrderItemRequestDto(products.get(name), quantity));
        }
        return orderService.create(new OrderCreateRequestDto(customerId, items).note(note)).getId();
    }
}
