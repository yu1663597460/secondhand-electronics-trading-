package com.secondhand.electronics.config;

import com.secondhand.electronics.entity.Category;
import com.secondhand.electronics.entity.Product;
import com.secondhand.electronics.entity.ProductStatus;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.entity.UserRole;
import com.secondhand.electronics.repository.CategoryRepository;
import com.secondhand.electronics.repository.ProductRepository;
import com.secondhand.electronics.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Profile({"h2", "mysql"})
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeData();
    }

    private void initializeData() {
        // 初始化分类
        if (categoryRepository.count() == 0) {
            createCategory("手机", "智能手机和功能手机");
            createCategory("电脑", "笔记本电脑和台式机");
            createCategory("平板", "平板电脑和电子书阅读器");
            createCategory("耳机", "有线和无线耳机");
            createCategory("相机", "数码相机和摄像设备");
            createCategory("游戏", "游戏机和游戏配件");
            createCategory("家电", "小家电和数码家电");
            createCategory("数码", "其他数码产品");
        }

        // 初始化管理员用户
        if (userRepository.findByUsername("admin").isEmpty()) {
            createUser("admin", "admin@example.com", "admin123", UserRole.ADMIN, "13800138000", "系统管理员");
        }

        // 初始化测试用户
        if (userRepository.findByUsername("testuser").isEmpty()) {
            createUser("testuser", "test@example.com", "123456", UserRole.USER, "13900139000", "测试用户");
        }

        // 初始化商品数据
        if (productRepository.count() == 0) {
            initializeProducts();
        }

        System.out.println("========================================");
        System.out.println("  H2数据库初始化完成！");
        System.out.println("  管理员账号: admin / admin123");
        System.out.println("  测试账号: testuser / 123456");
        System.out.println("  H2控制台: http://localhost:8080/api/h2-console");
        System.out.println("========================================");
    }

    private void createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        categoryRepository.save(category);
    }

    private void createUser(String username, String email, String password, UserRole role, String phone, String realName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setPhone(phone);
        user.setRealName(realName);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void initializeProducts() {
        Category phoneCategory = categoryRepository.findByName("手机").orElse(null);
        Category computerCategory = categoryRepository.findByName("电脑").orElse(null);
        Category tabletCategory = categoryRepository.findByName("平板").orElse(null);
        
        User testUser = userRepository.findByUsername("testuser").orElse(null);

        if (phoneCategory != null && testUser != null) {
            createProduct("iPhone 14 Pro Max 256GB 深空黑", "国行正品，99新，无拆无修，Face ID正常，电池健康度98%", 
                new BigDecimal("6899"), phoneCategory, testUser, "LIKE_NEW");
            createProduct("Samsung Galaxy S23 Ultra 512GB", "韩版无锁，S Pen功能完好，拍照效果出色，支持100倍变焦", 
                new BigDecimal("5999"), phoneCategory, testUser, "EXCELLENT");
        }

        if (computerCategory != null && testUser != null) {
            createProduct("MacBook Pro 14寸 M2 Pro 512GB", "2023款，几乎全新，保修期内，配原装充电器和包装盒", 
                new BigDecimal("12999"), computerCategory, testUser, "LIKE_NEW");
            createProduct("Dell XPS 13 Plus i7 32GB 1TB", "商务办公首选，轻薄便携，4K触摸屏，适合设计和开发", 
                new BigDecimal("9999"), computerCategory, testUser, "EXCELLENT");
        }

        if (tabletCategory != null && testUser != null) {
            createProduct("iPad Air 5 256GB WiFi版 蓝色", "轻度使用，9成新，配Apple Pencil 2代，学习办公神器", 
                new BigDecimal("3899"), tabletCategory, testUser, "GOOD");
        }
    }

    private void createProduct(String title, String description, BigDecimal price, 
                              Category category, User seller, String condition) {
        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setSeller(seller);
        product.setCondition(condition);
        product.setStatus(ProductStatus.APPROVED);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }
}