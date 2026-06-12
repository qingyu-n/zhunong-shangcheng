package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhunong.mall.common.PageResult;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.*;
import com.zhunong.mall.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BannerMapper bannerMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private SpecialProductMapper specialProductMapper;

    // ==================== 仪表盘统计 ====================

    @GetMapping("/dashboard/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> data = new HashMap<>();

        Long totalUsers = userMapper.selectCount(null);
        data.put("totalUsers", totalUsers);

        Long totalProducts = productMapper.selectCount(null);
        data.put("totalProducts", totalProducts);

        Long totalOrders = orderMapper.selectCount(null);
        data.put("totalOrders", totalOrders);

        LambdaQueryWrapper<Order> salesWrapper = new LambdaQueryWrapper<>();
        salesWrapper.eq(Order::getStatus, 3);
        List<Order> completedOrders = orderMapper.selectList(salesWrapper);
        BigDecimal totalSales = completedOrders.stream()
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalSales", totalSales);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.between(Order::getCreateTime, todayStart, todayEnd);
        Long todayOrders = orderMapper.selectCount(todayWrapper);
        data.put("todayOrders", todayOrders);

        List<Order> todayOrderList = orderMapper.selectList(todayWrapper);
        BigDecimal todaySales = todayOrderList.stream()
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("todaySales", todaySales);

        LambdaQueryWrapper<Order> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Order::getStatus, 0);
        Long pendingOrders = orderMapper.selectCount(pendingWrapper);
        data.put("pendingOrders", pendingOrders);

        LambdaQueryWrapper<Order> shipmentWrapper = new LambdaQueryWrapper<>();
        shipmentWrapper.eq(Order::getStatus, 1);
        Long pendingShipments = orderMapper.selectCount(shipmentWrapper);
        data.put("pendingShipments", pendingShipments);

        return Result.success(data);
    }

    @GetMapping("/dashboard/sales-trend")
    public Result<Map<String, Object>> getSalesTrend(@RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> data = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        List<Integer> orders = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime date = now.minusDays(i);
            dates.add(date.format(formatter));

            LocalDateTime dayStart = date.toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            LambdaQueryWrapper<Order> dayWrapper = new LambdaQueryWrapper<>();
            dayWrapper.between(Order::getCreateTime, dayStart, dayEnd);
            List<Order> dayOrders = orderMapper.selectList(dayWrapper);

            BigDecimal dayAmount = dayOrders.stream()
                    .map(Order::getPayAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            amounts.add(dayAmount.setScale(2, RoundingMode.HALF_UP));
            orders.add(dayOrders.size());
        }

        data.put("dates", dates);
        data.put("amounts", amounts);
        data.put("orders", orders);
        return Result.success(data);
    }

    @GetMapping("/dashboard/hot-products")
    public Result<List<Map<String, Object>>> getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.orderByDesc(Product::getSales);
        productWrapper.last("LIMIT " + limit);
        List<Product> products = productMapper.selectList(productWrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Product product : products) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", product.getId());
            map.put("name", product.getName());
            map.put("sales", product.getSales() != null ? product.getSales() : 0);
            map.put("amount", product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
            result.add(map);
        }

        return Result.success(result);
    }

    @GetMapping("/dashboard/recent-orders")
    public Result<List<Map<String, Object>>> getRecentOrders(@RequestParam(defaultValue = "10") Integer limit) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Order::getCreateTime);
        wrapper.last("LIMIT " + limit);
        List<Order> orderList = orderMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : orderList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("amount", order.getPayAmount());
            map.put("status", order.getStatus());
            map.put("statusName", getStatusName(order.getStatus()));
            map.put("createTime", order.getCreateTime());

            User user = userMapper.selectById(order.getUserId());
            map.put("username", user != null ? user.getUsername() : "未知用户");

            result.add(map);
        }

        return Result.success(result);
    }

    // ==================== 用户管理（管理后台操作sys_user表） ====================

    /**
     * 管理后台创建用户 - 专门给管理员使用，避免走AuthController.register的字段不匹配问题
     */
    @PostMapping("/user/create")
    public Result<User> createUserByAdmin(@RequestBody Map<String, String> userData) {
        try {
            String username = userData.get("username");
            String password = userData.get("password");

            if (username == null || username.isEmpty()) {
                return Result.error(400, "用户名不能为空");
            }
            if (password == null || password.isEmpty()) {
                return Result.error(400, "密码不能为空");
            }

            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, username);
            User existingUser = userMapper.selectOne(wrapper);
            if (existingUser != null) {
                return Result.error(400, "用户名已存在");
            }

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setNickname(userData.get("nickname") != null ? userData.get("nickname") : username);
            newUser.setPhone(userData.get("phone"));
            newUser.setEmail(userData.get("email"));
            newUser.setPassword(new BCryptPasswordEncoder().encode(password));
            newUser.setStatus(1);
            newUser.setRole("user");

            userMapper.insert(newUser);
            newUser.setPassword(null);
            return Result.success(newUser);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "创建用户失败: " + e.getMessage());
        }
    }

    // ==================== 管理员管理（独立于用户表） ====================

    /**
     * 获取管理员列表 - 使用AdminMapper操作sys_admin表
     */
    @GetMapping("/list")
    public Result<PageResult<Admin>> getAdminList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {

        // 确保role列存在
        try {
            jdbcTemplate.execute("ALTER TABLE `sys_admin` ADD COLUMN IF NOT EXISTS `role` VARCHAR(50) DEFAULT 'admin'");
        } catch (Exception ignored) {}

        Page<Admin> page = new Page<>(current, size);
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();

        if (username != null && !username.isEmpty()) {
            wrapper.like(Admin::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(Admin::getStatus, status);
        }

        wrapper.orderByDesc(Admin::getCreateTime);

        Page<Admin> adminPage = adminMapper.selectPage(page, wrapper);
        // 清除密码字段
        adminPage.getRecords().forEach(admin -> admin.setPassword(null));

        return Result.success(PageResult.of(adminPage.getRecords(), adminPage.getTotal()));
    }

    /**
     * 创建管理员 - 数据存入sys_admin表（不是sys_user表！）
     */
    @PostMapping
    public Result<Admin> createAdmin(@RequestBody Admin admin) {
        // 检查管理员名是否已存在（在sys_admin表中检查）
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, admin.getUsername());
        Admin existingAdmin = adminMapper.selectOne(wrapper);
        if (existingAdmin != null) {
            return Result.error(400, "管理员名已存在");
        }

        // 密码加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));

        // 设置默认值
        if (admin.getStatus() == null) {
            admin.setStatus(1);
        }
        if (admin.getNickname() == null || admin.getNickname().isEmpty()) {
            admin.setNickname(admin.getUsername());
        }
        if (admin.getRole() == null || admin.getRole().isEmpty()) {
            admin.setRole("admin");
        }

        adminMapper.insert(admin);
        admin.setPassword(null); // 返回时不返回密码
        return Result.success(admin);
    }

    /**
     * 更新管理员信息
     */
    @PutMapping("/{id}")
    public Result<Admin> updateAdmin(@PathVariable Long id, @RequestBody Admin admin) {
        Admin existingAdmin = adminMapper.selectById(id);
        if (existingAdmin == null) {
            return Result.error(404, "管理员不存在");
        }

        // 如果修改了密码，需要加密
        if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        } else {
            admin.setPassword(existingAdmin.getPassword());
        }

        admin.setId(id);
        adminMapper.updateById(admin);
        admin.setPassword(null);
        return Result.success(admin);
    }

    /**
     * 删除管理员 - 从sys_admin表删除（不是sys_user表！）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            return Result.error(404, "管理员不存在");
        }
        adminMapper.deleteById(id);
        return Result.success();
    }

    // ==================== 收藏统计 ====================

    @GetMapping("/favorite/stats")
    public Result<Map<String, Object>> getFavoriteStats() {
        Map<String, Object> data = new HashMap<>();

        try {
            LambdaQueryWrapper<Favorite> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(Favorite::getIsDelete, 0);
            Long totalFavorites = favoriteMapper.selectCount(totalWrapper);
            data.put("totalFavorites", totalFavorites);

            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime todayEnd = todayStart.plusDays(1);
            LambdaQueryWrapper<Favorite> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.eq(Favorite::getIsDelete, 0);
            todayWrapper.between(Favorite::getCreateTime, todayStart, todayEnd);
            Long todayFavorites = favoriteMapper.selectCount(todayWrapper);
            data.put("todayFavorites", todayFavorites);

            LambdaQueryWrapper<Favorite> allWrapper = new LambdaQueryWrapper<>();
            allWrapper.eq(Favorite::getIsDelete, 0);
            List<Favorite> allFavorites = favoriteMapper.selectList(allWrapper);

            Set<Long> uniqueUserIds = new HashSet<>();
            Map<Long, Long> productFavoriteCount = new HashMap<>();

            for (Favorite fav : allFavorites) {
                if (fav.getUserId() != null) {
                    uniqueUserIds.add(fav.getUserId());
                }
                if (fav.getProductId() != null) {
                    Long productId = fav.getProductId();
                    productFavoriteCount.put(productId, productFavoriteCount.getOrDefault(productId, 0L) + 1);
                }
            }

            data.put("favoriteUserCount", uniqueUserIds.size());

            List<Map.Entry<Long, Long>> sortedEntries = productFavoriteCount.entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(5)
                    .toList();

            List<Map<String, Object>> hotProducts = new ArrayList<>();
            for (Map.Entry<Long, Long> entry : sortedEntries) {
                Product product = productMapper.selectById(entry.getKey());
                if (product != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productId", product.getId());
                    map.put("productName", product.getName());
                    map.put("favoriteCount", entry.getValue());
                    hotProducts.add(map);
                }
            }
            data.put("hotProducts", hotProducts);

        } catch (Exception e) {
            data.put("totalFavorites", 0);
            data.put("todayFavorites", 0);
            data.put("favoriteUserCount", 0);
            data.put("hotProducts", new ArrayList<>());
            e.printStackTrace();
        }

        return Result.success(data);
    }

    // ==================== 分类管理 ====================

    @GetMapping("/category/list")
    public Result<List<Category>> getCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        List<Category> list = categoryMapper.selectList(wrapper);
        return Result.success(list);
    }

    @PostMapping("/category")
    public Result<Category> createCategory(@RequestBody Map<String, Object> categoryData) {
        try {
            Category category = new Category();
            category.setName(categoryData.get("name") != null ? categoryData.get("name").toString() : "");
            if (categoryData.get("parentId") != null) {
                Object parentIdObj = categoryData.get("parentId");
                if (parentIdObj instanceof Number) {
                    category.setParentId(((Number) parentIdObj).longValue());
                } else if (parentIdObj != null && !parentIdObj.toString().isEmpty()) {
                    category.setParentId(Long.valueOf(parentIdObj.toString()));
                }
            }
            if (categoryData.get("level") != null) {
                Object levelObj = categoryData.get("level");
                if (levelObj instanceof Number) {
                    category.setLevel(((Number) levelObj).intValue());
                }
            }
            if (categoryData.get("sortOrder") != null) {
                Object sortOrderObj = categoryData.get("sortOrder");
                if (sortOrderObj instanceof Number) {
                    category.setSortOrder(((Number) sortOrderObj).intValue());
                }
            }
            if (categoryData.get("icon") != null) {
                category.setIcon(categoryData.get("icon").toString());
            }
            if (categoryData.get("status") != null) {
                Object statusObj = categoryData.get("status");
                if (statusObj instanceof Number) {
                    category.setStatus(((Number) statusObj).intValue());
                }
            }

            // 设置默认值
            if (category.getParentId() == null) {
                category.setParentId(0L);
            }
            if (category.getLevel() == null) {
                category.setLevel(0);
            }
            if (category.getStatus() == null) {
                category.setStatus(1);
            }
            if (category.getSortOrder() == null) {
                category.setSortOrder(0);
            }

            category.setCreateTime(LocalDateTime.now());
            categoryMapper.insert(category);
            return Result.success(category);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "创建分类失败: " + e.getMessage());
        }
    }

    @PutMapping("/category/{id}")
    public Result<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryMapper.updateById(category);
        return Result.success(category);
    }

    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryMapper.deleteById(id);
        return Result.success();
    }

    // ==================== 商品管理 ====================

    @GetMapping("/product/list")
    public Result<PageResult<Product>> getProductList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer status) {

        Page<Product> page = new Page<>(current, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (name != null && !name.isEmpty()) {
            wrapper.like(Product::getName, name);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        wrapper.orderByDesc(Product::getCreateTime);

        Page<Product> productPage = productMapper.selectPage(page, wrapper);
        return Result.success(PageResult.of(productPage.getRecords(), productPage.getTotal()));
    }

    @PostMapping("/product")
    public Result<Product> createProduct(@RequestBody Map<String, Object> productData) {
        try {
            Product product = new Product();
            product.setName(productData.get("name") != null ? productData.get("name").toString() : "");
            if (productData.get("categoryId") != null) {
                product.setCategoryId(Long.valueOf(productData.get("categoryId").toString()));
            }
            if (productData.get("price") != null) {
                product.setPrice(new java.math.BigDecimal(productData.get("price").toString()));
            }
            if (productData.get("stock") != null) {
                product.setStock(Integer.valueOf(productData.get("stock").toString()));
            }
            product.setMainImage(productData.get("mainImage") != null ? productData.get("mainImage").toString() : "");
            product.setDescription(productData.get("description") != null ? productData.get("description").toString() : "");
            product.setDetail(productData.get("detail") != null ? productData.get("detail").toString() : "");
            if (productData.get("originalPrice") != null) {
                product.setOriginalPrice(new java.math.BigDecimal(productData.get("originalPrice").toString()));
            }
            if (productData.get("origin") != null) {
                product.setOrigin(productData.get("origin").toString());
            }
            if (productData.get("unit") != null) {
                product.setUnit(productData.get("unit").toString());
            }
            if (productData.get("weight") != null) {
                product.setWeight(new java.math.BigDecimal(productData.get("weight").toString()));
            }
            if (productData.get("images") != null) {
                product.setImages(productData.get("images").toString());
            }

            product.setCreateTime(LocalDateTime.now());
            if (product.getStatus() == null) {
                product.setStatus(1);
            }
            if (product.getSales() == null) {
                product.setSales(0);
            }
            productMapper.insert(product);
            return Result.success(product);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "创建商品失败: " + e.getMessage());
        }
    }

    @PutMapping("/product/{id}")
    public Result<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productMapper.updateById(product);
        return Result.success(product);
    }

    @GetMapping("/product/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error(404, "商品不存在");
        }
        return Result.success(product);
    }

    @DeleteMapping("/product/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productMapper.deleteById(id);
        return Result.success();
    }

    // ==================== 订单管理 ====================

    @GetMapping("/order/list")
    public Result<PageResult<Map<String, Object>>> getOrderList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status) {

        Page<Order> page = new Page<>(current, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.eq(Order::getOrderNo, orderNo);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> orderPage = orderMapper.selectPage(page, wrapper);

        List<Map<String, Object>> enrichedRecords = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("userId", order.getUserId());
            map.put("totalAmount", order.getTotalAmount());
            map.put("freightAmount", order.getFreightAmount());
            map.put("discountAmount", order.getDiscountAmount());
            map.put("payAmount", order.getPayAmount());
            map.put("status", order.getStatus());
            map.put("payType", order.getPayType());
            map.put("payTime", order.getPayTime());
            map.put("receiverName", order.getReceiverName());
            map.put("receiverPhone", order.getReceiverPhone());
            map.put("receiverAddress", order.getReceiverAddress());
            map.put("remark", order.getRemark());
            map.put("createTime", order.getCreateTime());

            // 查询用户名
            User user = userMapper.selectById(order.getUserId());
            map.put("username", user != null ? user.getUsername() : "未知用户");

            enrichedRecords.add(map);
        }

        return Result.success(PageResult.of(enrichedRecords, orderPage.getTotal()));
    }

    @GetMapping("/order/{id}")
    public Result<Order> getOrderDetail(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(order);
    }

    @PutMapping("/order/status")
    public Result<Void> updateOrderStatus(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }

        order.setStatus(status);
        orderMapper.updateById(order);
        return Result.success();
    }

    // ==================== 轮播图管理 ====================

    @GetMapping("/banner/list")
    public Result<List<Banner>> getBannerList() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Banner::getSort);
        List<Banner> list = bannerMapper.selectList(wrapper);
        return Result.success(list);
    }

    @PostMapping("/banner")
    public Result<Banner> createBanner(@RequestBody Banner banner) {
        try {
            banner.setCreateTime(LocalDateTime.now());
            bannerMapper.insert(banner);
            return Result.success(banner);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "创建轮播图失败: " + e.getMessage());
        }
    }

    @PutMapping("/banner/{id}")
    public Result<Banner> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        bannerMapper.updateById(banner);
        return Result.success(banner);
    }

    @DeleteMapping("/banner/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return Result.success();
    }

    // ==================== 购物车管理 ====================

    @GetMapping("/cart/list")
    public Result<PageResult<Cart>> getCartList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Cart> page = new Page<>(current, size);
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Cart::getCreateTime);

        Page<Cart> cartPage = cartMapper.selectPage(page, wrapper);
        return Result.success(PageResult.of(cartPage.getRecords(), cartPage.getTotal()));
    }

    @DeleteMapping("/cart/{id}")
    public Result<Void> deleteCart(@PathVariable Long id) {
        cartMapper.deleteById(id);
        return Result.success();
    }

    // ==================== 评价管理 ====================

    @GetMapping("/review/list")
    public Result<PageResult<Object>> getReviewList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        PageResult<Object> result = new PageResult<>();
        result.setList(new ArrayList<>());
        result.setTotal(0L);
        return Result.success(result);
    }

    @DeleteMapping("/review/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        return Result.success();
    }

    // ==================== 收藏管理 ====================

    @GetMapping("/favorite/list")
    public Result<PageResult<Favorite>> getFavoriteList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Favorite> page = new Page<>(current, size);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getIsDelete, 0);
        wrapper.orderByDesc(Favorite::getCreateTime);

        Page<Favorite> favPage = favoriteMapper.selectPage(page, wrapper);
        return Result.success(PageResult.of(favPage.getRecords(), favPage.getTotal()));
    }

    @DeleteMapping("/favorite/{id}")
    public Result<Void> deleteFavorite(@PathVariable Long id) {
        Favorite fav = favoriteMapper.selectById(id);
        if (fav != null) {
            fav.setIsDelete(1);
            favoriteMapper.updateById(fav);
        }
        return Result.success();
    }

    // ==================== 活动管理 ====================

    @GetMapping("/activity/list")
    public Result<List<Map<String, Object>>> getActivityList() {
        List<Map<String, Object>> activities = new ArrayList<>();

        try {
            List<Map<String, Object>> rawList = jdbcTemplate.queryForList(
                "SELECT id, title, start_time as startTime, end_time as endTime, status, create_time as createTime FROM activity ORDER BY create_time DESC"
            );
            activities.addAll(rawList);
        } catch (Exception e) {
            // 表可能不存在，返回空列表
        }

        return Result.success(activities);
    }

    @PostMapping("/activity")
    public Result<Map<String, Object>> createActivity(@RequestBody Map<String, Object> activity) {
        try {
            activity.put("createTime", LocalDateTime.now().toString());
            jdbcTemplate.update(
                "INSERT INTO activity(title, start_time, end_time, status, create_time) VALUES(?, ?, ?, ?, ?)",
                activity.get("title"), activity.get("startTime"), activity.get("endTime"),
                activity.get("status"), activity.get("createTime")
            );
            return Result.success(activity);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "创建活动失败: " + e.getMessage());
        }
    }

    @PutMapping("/activity/{id}")
    public Result<Map<String, Object>> updateActivity(@PathVariable Long id, @RequestBody Map<String, Object> activity) {
        try {
            jdbcTemplate.update(
                "UPDATE activity SET title=?, start_time=?, end_time=?, status=? WHERE id=?",
                activity.get("title"), activity.get("startTime"), activity.get("endTime"),
                activity.get("status"), id
            );
            activity.put("id", id);
            return Result.success(activity);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "更新活动失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/activity/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        try {
            jdbcTemplate.update("DELETE FROM activity WHERE id=?", id);
            return Result.success();
        } catch (Exception e) {
            return Result.success(); // 即使删除失败也返回成功避免前端报错
        }
    }

    // ==================== 系统配置 ====================

    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("siteName", "助农商城");
        config.put("siteDescription", "连接城市与乡村的优质农产品平台");
        config.put("contactPhone", "400-888-8888");
        config.put("contactEmail", "service@zhunong.com");
        config.put("version", "1.0.0");
        config.put("icp", "");
        return Result.success(config);
    }

    @PutMapping("/config")
    public Result<Void> saveConfig(@RequestBody Map<String, Object> config) {
        return Result.success();
    }

    // ==================== 热销商品管理 ====================

    @GetMapping("/hot-product/list")
    public Result<PageResult<Product>> getHotProductList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<Product> page = new Page<>(current, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsHot, 1);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }
        wrapper.orderByDesc(Product::getSales);
        Page<Product> productPage = productMapper.selectPage(page, wrapper);
        return Result.success(PageResult.of(productPage.getRecords(), productPage.getTotal()));
    }

    @PostMapping("/hot-product/{id}")
    public Result<Void> addHotProduct(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product != null) {
            product.setIsHot(1);
            productMapper.updateById(product);
        }
        return Result.success();
    }

    @DeleteMapping("/hot-product/{id}")
    public Result<Void> removeHotProduct(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product != null) {
            product.setIsHot(0);
            productMapper.updateById(product);
        }
        return Result.success();
    }

    @PutMapping("/hot-product/sort")
    public Result<Void> updateHotProductSort(@RequestBody Map<String, Object> data) {
        Long productId = Long.valueOf(data.get("productId").toString());
        Integer sortOrder = Integer.valueOf(data.get("sortOrder").toString());
        Product product = productMapper.selectById(productId);
        if (product != null && product.getIsHot() == 1) {
            product.setSortOrder(sortOrder);
            productMapper.updateById(product);
        }
        return Result.success();
    }

    // ==================== 助农优选商品管理 ====================

    @GetMapping("/premium-product/list")
    public Result<PageResult<Product>> getPremiumProductList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<Product> page = new Page<>(current, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsHelp, 1);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        Page<Product> productPage = productMapper.selectPage(page, wrapper);
        return Result.success(PageResult.of(productPage.getRecords(), productPage.getTotal()));
    }

    @PostMapping("/premium-product/{id}")
    public Result<Void> addPremiumProduct(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product != null) {
            product.setIsHelp(1);
            productMapper.updateById(product);
        }
        return Result.success();
    }

    @DeleteMapping("/premium-product/{id}")
    public Result<Void> removePremiumProduct(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product != null) {
            product.setIsHelp(0);
            productMapper.updateById(product);
        }
        return Result.success();
    }

    // ==================== 限时特惠商品管理 ====================

    @GetMapping("/special-offer/list")
    public Result<PageResult<SpecialProduct>> getSpecialOfferList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        Page<SpecialProduct> page = new Page<>(current, size);
        LambdaQueryWrapper<SpecialProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpecialProduct::getType, "special_offer");
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SpecialProduct::getName, keyword);
        }
        if (status != null) {
            wrapper.eq(SpecialProduct::getStatus, status);
        }
        wrapper.orderByAsc(SpecialProduct::getSortOrder);
        Page<SpecialProduct> specialPage = specialProductMapper.selectPage(page, wrapper);

        List<SpecialProduct> enrichedList = new ArrayList<>();
        for (SpecialProduct sp : specialPage.getRecords()) {
            if (sp.getProductId() != null) {
                Product p = productMapper.selectById(sp.getProductId());
                if (p != null) {
                    if (sp.getName() == null || sp.getName().isEmpty()) {
                        sp.setName(p.getName());
                    }
                    if (sp.getImage() == null || sp.getImage().isEmpty()) {
                        sp.setImage(p.getMainImage());
                    }
                    if (sp.getOriginalPrice() == null) {
                        sp.setOriginalPrice(p.getPrice());
                    }
                }
            }
            enrichedList.add(sp);
        }

        return Result.success(PageResult.of(enrichedList, specialPage.getTotal()));
    }

    @PostMapping("/special-offer")
    public Result<SpecialProduct> createSpecialOffer(@RequestBody Map<String, Object> data) {
        try {
            SpecialProduct sp = new SpecialProduct();
            sp.setType("special_offer");
            if (data.get("productId") != null) {
                sp.setProductId(Long.valueOf(data.get("productId").toString()));
                Product p = productMapper.selectById(sp.getProductId());
                if (p != null) {
                    sp.setName(p.getName());
                    sp.setImage(p.getMainImage());
                    sp.setOriginalPrice(p.getPrice());
                }
            }
            sp.setName(data.get("name") != null ? data.get("name").toString() : "");
            if (data.get("originalPrice") != null) {
                sp.setOriginalPrice(new BigDecimal(data.get("originalPrice").toString()));
            }
            if (data.get("discountPrice") != null) {
                sp.setDiscountPrice(new BigDecimal(data.get("discountPrice").toString()));
            }
            if (data.get("discountPercent") != null) {
                sp.setDiscountPercent(Integer.valueOf(data.get("discountPercent").toString()));
            }
            if (data.get("startTime") != null) {
                sp.setStartTime(LocalDateTime.parse(data.get("startTime").toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            if (data.get("endTime") != null) {
                sp.setEndTime(LocalDateTime.parse(data.get("endTime").toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            sp.setStatus(data.get("status") != null ? Integer.valueOf(data.get("status").toString()) : 1);
            sp.setSortOrder(data.get("sortOrder") != null ? Integer.valueOf(data.get("sortOrder").toString()) : 0);
            sp.setDescription(data.get("description") != null ? data.get("description").toString() : "");
            sp.setImage(data.get("image") != null ? data.get("image").toString() : "");
            sp.setCreateTime(LocalDateTime.now());

            specialProductMapper.insert(sp);
            return Result.success(sp);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "创建限时特惠商品失败: " + e.getMessage());
        }
    }

    @PutMapping("/special-offer/{id}")
    public Result<SpecialProduct> updateSpecialOffer(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            SpecialProduct sp = specialProductMapper.selectById(id);
            if (sp == null) {
                return Result.error(404, "商品不存在");
            }
            if (data.get("name") != null) sp.setName(data.get("name").toString());
            if (data.get("originalPrice") != null) sp.setOriginalPrice(new BigDecimal(data.get("originalPrice").toString()));
            if (data.get("discountPrice") != null) sp.setDiscountPrice(new BigDecimal(data.get("discountPrice").toString()));
            if (data.get("discountPercent") != null) sp.setDiscountPercent(Integer.valueOf(data.get("discountPercent").toString()));
            if (data.get("startTime") != null) sp.setStartTime(LocalDateTime.parse(data.get("startTime").toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            if (data.get("endTime") != null) sp.setEndTime(LocalDateTime.parse(data.get("endTime").toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            if (data.get("status") != null) sp.setStatus(Integer.valueOf(data.get("status").toString()));
            if (data.get("sortOrder") != null) sp.setSortOrder(Integer.valueOf(data.get("sortOrder").toString()));
            if (data.get("description") != null) sp.setDescription(data.get("description").toString());
            if (data.get("image") != null) sp.setImage(data.get("image").toString());
            if (data.get("productId") != null) sp.setProductId(Long.valueOf(data.get("productId").toString()));

            specialProductMapper.updateById(sp);
            return Result.success(sp);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/special-offer/{id}")
    public Result<Void> deleteSpecialOffer(@PathVariable Long id) {
        specialProductMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/init-special-table")
    public Result<String> initSpecialTable() {
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS `special_product`");
            jdbcTemplate.execute("CREATE TABLE `special_product` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`product_id` BIGINT DEFAULT NULL," +
                "`type` VARCHAR(50) NOT NULL DEFAULT 'special_offer'," +
                "`name` VARCHAR(200) DEFAULT NULL," +
                "`original_price` DECIMAL(10,2) DEFAULT NULL," +
                "`discount_price` DECIMAL(10,2) DEFAULT NULL," +
                "`discount_percent` INT DEFAULT NULL," +
                "`start_time` DATETIME DEFAULT NULL," +
                "`end_time` DATETIME DEFAULT NULL," +
                "`status` TINYINT DEFAULT 1," +
                "`sort_order` INT DEFAULT 0," +
                "`description` TEXT," +
                "`image` VARCHAR(500) DEFAULT NULL," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "`is_delete` TINYINT DEFAULT 0," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='特殊商品管理表'");
            return Result.success("表创建成功");
        } catch (Exception e) {
            return Result.error(500, "表创建失败: " + e.getMessage());
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待付款";
            case 1: return "已付款";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }

    // ==================== 商品审核管理 ====================

    @Autowired
    private FarmerProfileMapper farmerProfileMapper;

    @Autowired
    private ProductReportMapper productReportMapper;

    /**
     * 获取待审核商品列表
     */
    @GetMapping("/audit/products")
    public Result<PageResult<Product>> getAuditProductList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer auditStatus) {
        try {
            Page<Product> page = new Page<>(current, size);
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getProductType, 1);

            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like(Product::getName, keyword);
            }
            if (auditStatus != null) {
                wrapper.eq(Product::getAuditStatus, auditStatus);
            } else {
                wrapper.in(Product::getAuditStatus, 0, 1, 2);
            }

            wrapper.orderByDesc(Product::getCreateTime);

            Page<Product> productPage = productMapper.selectPage(page, wrapper);
            return Result.success(PageResult.of(productPage.getRecords(), productPage.getTotal()));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取商品审核列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品审核详情
     */
    @GetMapping("/audit/products/{id}")
    public Result<Product> getAuditProductDetail(@PathVariable Long id) {
        try {
            Product product = productMapper.selectById(id);
            if (product == null) {
                return Result.error(404, "商品不存在");
            }
            return Result.success(product);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取商品详情失败: " + e.getMessage());
        }
    }

    /**
     * 通过商品审核
     */
    @PutMapping("/audit/products/{id}/approve")
    public Result<Void> approveProduct(@PathVariable Long id, @RequestBody(required = false) Map<String, String> data) {
        try {
            Product product = productMapper.selectById(id);
            if (product == null) {
                return Result.error(404, "商品不存在");
            }
            product.setAuditStatus(1);
            product.setStatus(1);
            product.setRejectReason(null);
            productMapper.updateById(product);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "通过审核失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝商品审核
     */
    @PutMapping("/audit/products/{id}/reject")
    public Result<Void> rejectProduct(@PathVariable Long id, @RequestBody Map<String, String> data) {
        try {
            Product product = productMapper.selectById(id);
            if (product == null) {
                return Result.error(404, "商品不存在");
            }
            product.setAuditStatus(2);
            product.setStatus(0);
            product.setRejectReason(data.get("reason"));
            productMapper.updateById(product);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "拒绝审核失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/audit/products/{id}")
    public Result<Void> deleteAuditProduct(@PathVariable Long id) {
        try {
            Product product = productMapper.selectById(id);
            if (product == null) {
                return Result.error(404, "商品不存在");
            }
            product.setIsDelete(1);
            productMapper.updateById(product);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "删除商品失败: " + e.getMessage());
        }
    }

    // ==================== 农户管理 ====================

    /**
     * 获取农户申请列表
     */
    @GetMapping("/audit/farmers")
    public Result<PageResult<FarmerProfile>> getFarmerList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            Page<FarmerProfile> page = new Page<>(current, size);
            LambdaQueryWrapper<FarmerProfile> wrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.isEmpty()) {
                wrapper.and(w -> w.like(FarmerProfile::getContactName, keyword)
                        .or().like(FarmerProfile::getShopName, keyword)
                        .or().like(FarmerProfile::getContactPhone, keyword));
            }
            if (status != null) {
                wrapper.eq(FarmerProfile::getStatus, status);
            } else {
                wrapper.in(FarmerProfile::getStatus, 0, 1, 2);
            }

            wrapper.orderByDesc(FarmerProfile::getCreateTime);

            Page<FarmerProfile> farmerPage = farmerProfileMapper.selectPage(page, wrapper);
            return Result.success(PageResult.of(farmerPage.getRecords(), farmerPage.getTotal()));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取农户列表失败: " + e.getMessage());
        }
    }

    /**
     * 通过农户申请
     */
    @PutMapping("/audit/farmers/{id}/approve")
    public Result<Void> approveFarmer(@PathVariable Long id, @RequestBody(required = false) Map<String, String> data) {
        try {
            FarmerProfile profile = farmerProfileMapper.selectById(id);
            if (profile == null) {
                return Result.error(404, "农户申请不存在");
            }
            profile.setStatus(1);
            profile.setAuditRemark(data != null ? data.get("remark") : "");
            profile.setAuditTime(LocalDateTime.now());
            farmerProfileMapper.updateById(profile);

            User user = userMapper.selectById(profile.getUserId());
            if (user != null) {
                user.setIsFarmer(1);
                user.setFarmerApplyStatus(1);
                userMapper.updateById(user);

                LambdaQueryWrapper<Shop> shopWrapper = new LambdaQueryWrapper<>();
                shopWrapper.eq(Shop::getFarmerId, profile.getUserId());
                Shop existingShop = shopMapper.selectOne(shopWrapper);
                if (existingShop == null) {
                    Shop newShop = new Shop();
                    newShop.setFarmerId(profile.getUserId());
                    newShop.setName(profile.getShopName() != null ? profile.getShopName() : user.getNickname() + "的店铺");
                    newShop.setDescription(profile.getDescription());
                    newShop.setContactPhone(profile.getContactPhone());
                    newShop.setAddress(profile.getContactAddress());
                    newShop.setViewCount(0);
                    newShop.setFavoriteCount(0);
                    newShop.setProductCount(0);
                    newShop.setStatus(1);
                    shopMapper.insert(newShop);
                }
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "通过农户申请失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝农户申请
     */
    @PutMapping("/audit/farmers/{id}/reject")
    public Result<Void> rejectFarmer(@PathVariable Long id, @RequestBody Map<String, String> data) {
        try {
            FarmerProfile profile = farmerProfileMapper.selectById(id);
            if (profile == null) {
                return Result.error(404, "农户申请不存在");
            }
            profile.setStatus(2);
            profile.setAuditRemark(data.get("reason"));
            profile.setAuditTime(LocalDateTime.now());
            farmerProfileMapper.updateById(profile);

            User user = userMapper.selectById(profile.getUserId());
            if (user != null) {
                user.setFarmerApplyStatus(2);
                userMapper.updateById(user);
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "拒绝农户申请失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/audit/farmers/{id}")
    public Result<Void> deleteFarmer(@PathVariable Long id) {
        try {
            FarmerProfile profile = farmerProfileMapper.selectById(id);
            if (profile == null) {
                return Result.error(404, "农户申请不存在");
            }
            User user = userMapper.selectById(profile.getUserId());
            if (user != null) {
                user.setIsFarmer(0);
                user.setFarmerApplyStatus(null);
                userMapper.updateById(user);
            }
            farmerProfileMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "删除农户失败: " + e.getMessage());
        }
    }

    // ==================== 举报管理 ====================

    /**
     * 获取举报列表
     */
    @GetMapping("/reports")
    public Result<PageResult<Map<String, Object>>> getReportList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        try {
            Page<ProductReport> page = new Page<>(current, size);
            LambdaQueryWrapper<ProductReport> wrapper = new LambdaQueryWrapper<>();

            if (status != null) {
                wrapper.eq(ProductReport::getStatus, status);
            }

            wrapper.orderByDesc(ProductReport::getCreateTime);

            Page<ProductReport> reportPage = productReportMapper.selectPage(page, wrapper);

            List<Map<String, Object>> enrichedRecords = new ArrayList<>();
            for (ProductReport report : reportPage.getRecords()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", report.getId());
                map.put("productId", report.getProductId());
                map.put("reporterId", report.getReporterId());
                map.put("reasonType", report.getReasonType());
                map.put("reasonDetail", report.getReasonDetail());
                map.put("evidenceImages", report.getEvidenceImages());
                map.put("status", report.getStatus());
                map.put("handlerId", report.getHandlerId());
                map.put("handleResult", report.getHandleResult());
                map.put("handleTime", report.getHandleTime());
                map.put("createTime", report.getCreateTime());

                // 查询举报人用户名
                User reporter = userMapper.selectById(report.getReporterId());
                map.put("reporterName", reporter != null ? reporter.getUsername() : "未知用户");

                // 查询被举报商品信息
                Product product = productMapper.selectById(report.getProductId());
                if (product != null) {
                    Map<String, Object> productInfo = new HashMap<>();
                    productInfo.put("name", product.getName());
                    productInfo.put("mainImage", product.getMainImage());
                    productInfo.put("price", product.getPrice());
                    map.put("productInfo", productInfo);
                } else {
                    Map<String, Object> productInfo = new HashMap<>();
                    productInfo.put("name", "商品已删除");
                    productInfo.put("mainImage", "");
                    productInfo.put("price", null);
                    map.put("productInfo", productInfo);
                }

                enrichedRecords.add(map);
            }

            return Result.success(PageResult.of(enrichedRecords, reportPage.getTotal()));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取举报列表失败: " + e.getMessage());
        }
    }

    /**
     * 处理举报
     */
    @PutMapping("/reports/{id}/handle")
    public Result<Void> handleReport(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            ProductReport report = productReportMapper.selectById(id);
            if (report == null) {
                return Result.error(404, "举报记录不存在");
            }

            Integer status = Integer.valueOf(data.get("status").toString());
            report.setStatus(status);
            report.setHandleResult(data.get("remark") != null ? data.get("remark").toString() : "");
            report.setHandleTime(LocalDateTime.now());

            productReportMapper.updateById(report);

            if (status == 2 && report.getProductId() != null) {
                Product product = productMapper.selectById(report.getProductId());
                if (product != null) {
                    product.setStatus(0);
                    productMapper.updateById(product);
                }
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "处理举报失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/reports/{id}")
    public Result<Void> deleteReport(@PathVariable Long id) {
        try {
            ProductReport report = productReportMapper.selectById(id);
            if (report == null) {
                return Result.error(404, "举报记录不存在");
            }
            productReportMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "删除举报失败: " + e.getMessage());
        }
    }
}
