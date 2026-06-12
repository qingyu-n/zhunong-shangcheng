package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.mapper.OrderItemMapper;
import com.zhunong.mall.mapper.OrderMapper;
import com.zhunong.mall.mapper.ProductMapper;
import com.zhunong.mall.mapper.ShopMapper;
import com.zhunong.mall.entity.Order;
import com.zhunong.mall.entity.OrderItem;
import com.zhunong.mall.entity.Product;
import com.zhunong.mall.entity.Shop;
import com.zhunong.mall.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/farmer/statistics")
public class StatisticsController {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromHeader(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }

    private List<Long> getFarmerProductIds(Long userId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getFarmerId, userId).select(Product::getId);
        return productMapper.selectList(wrapper).stream().map(Product::getId).collect(Collectors.toList());
    }

    private List<Long> getFarmerOrderIds(Long userId) {
        List<Long> productIds = getFarmerProductIds(userId);
        if (productIds.isEmpty()) return Collections.emptyList();
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getProductId, productIds).select(OrderItem::getOrderId);
        return orderItemMapper.selectList(itemWrapper).stream().map(OrderItem::getOrderId).distinct().collect(Collectors.toList());
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Map<String, Object> overview = new HashMap<>();

        Long productCount = productMapper.selectCount(
            new LambdaQueryWrapper<Product>()
                .eq(Product::getFarmerId, userId)
                .eq(Product::getIsDelete, 0)
        );
        overview.put("totalProducts", productCount);

        Shop shop = shopMapper.selectOne(
            new LambdaQueryWrapper<Shop>()
                .eq(Shop::getFarmerId, userId)
                .eq(Shop::getIsDelete, 0)
        );
        if (shop != null) {
            overview.put("totalViews", shop.getViewCount() != null ? shop.getViewCount() : 0);
            overview.put("totalFavorite", shop.getFavoriteCount() != null ? shop.getFavoriteCount() : 0);
        } else {
            overview.put("totalViews", 0);
            overview.put("totalFavorite", 0);
        }

        List<Long> orderIds = getFarmerOrderIds(userId);
        if (orderIds.isEmpty()) {
            overview.put("totalSales", BigDecimal.ZERO);
            overview.put("totalOrders", 0);
            overview.put("todaySales", BigDecimal.ZERO);
            overview.put("todayOrders", 0);
            overview.put("todayVisitors", 0);
            return Result.success(overview);
        }

        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.in(Order::getId, orderIds).eq(Order::getIsDelete, 0);
        List<Order> allOrders = orderMapper.selectList(orderWrapper);

        BigDecimal totalSales = allOrders.stream()
            .filter(o -> o.getStatus() != null && o.getStatus() != 4)
            .map(Order::getPayAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        overview.put("totalSales", totalSales);
        overview.put("totalOrders", allOrders.size());

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        List<Order> todayOrders = allOrders.stream()
            .filter(o -> o.getCreateTime() != null && o.getCreateTime().isAfter(todayStart) && o.getCreateTime().isBefore(todayEnd))
            .collect(Collectors.toList());
        BigDecimal todaySales = todayOrders.stream()
            .filter(o -> o.getStatus() != null && o.getStatus() != 4)
            .map(Order::getPayAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        overview.put("todaySales", todaySales);
        overview.put("todayOrders", todayOrders.size());
        overview.put("todayVisitors", 0);

        return Result.success(overview);
    }

    @GetMapping("/sales")
    public Result<Map<String, Object>> getSalesTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        List<Long> orderIds = getFarmerOrderIds(userId);
        Map<String, Object> result = new HashMap<>();

        if (orderIds.isEmpty()) {
            result.put("list", Collections.emptyList());
            return Result.success(result);
        }

        int days = 7;
        LocalDate endLocalDate = LocalDate.now();
        LocalDate startLocalDate = endLocalDate.minusDays(days - 1);

        if (startDate != null && endDate != null) {
            try {
                startLocalDate = LocalDate.parse(startDate);
                endLocalDate = LocalDate.parse(endDate);
                days = (int) java.time.temporal.ChronoUnit.DAYS.between(startLocalDate, endLocalDate) + 1;
            } catch (Exception ignored) {}
        }

        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.in(Order::getId, orderIds).eq(Order::getIsDelete, 0)
            .ge(Order::getCreateTime, startLocalDate.atStartOfDay())
            .le(Order::getCreateTime, endLocalDate.plusDays(1).atStartOfDay());
        List<Order> orders = orderMapper.selectList(orderWrapper);

        List<Map<String, Object>> list = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < days; i++) {
            LocalDate date = startLocalDate.plusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            List<Order> dayOrders = orders.stream()
                .filter(o -> o.getCreateTime() != null && o.getCreateTime().isAfter(dayStart) && o.getCreateTime().isBefore(dayEnd))
                .collect(Collectors.toList());

            BigDecimal daySales = dayOrders.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() != 4)
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(formatter));
            item.put("sales", daySales);
            item.put("orders", dayOrders.size());
            item.put("products", 0);
            item.put("growth", 0);
            list.add(item);
        }

        for (int i = 1; i < list.size(); i++) {
            BigDecimal prevSales = (BigDecimal) list.get(i - 1).get("sales");
            BigDecimal currSales = (BigDecimal) list.get(i).get("sales");
            if (prevSales.compareTo(BigDecimal.ZERO) > 0) {
                int growth = currSales.subtract(prevSales)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(prevSales, 0, java.math.RoundingMode.HALF_UP).intValue();
                list.get(i).put("growth", growth);
            }
        }

        result.put("list", list);
        return Result.success(result);
    }

    @GetMapping("/products")
    public Result<Map<String, Object>> getProductStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Map<String, Object> result = new HashMap<>();

        List<Long> productIds = getFarmerProductIds(userId);
        if (productIds.isEmpty()) {
            result.put("topProducts", Collections.emptyList());
            result.put("orderStats", Map.of());
            return Result.success(result);
        }

        List<Long> orderIds = getFarmerOrderIds(userId);

        Map<Long, Integer> productSalesCount = new HashMap<>();
        Map<Long, BigDecimal> productSalesAmount = new HashMap<>();
        if (!orderIds.isEmpty()) {
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(OrderItem::getOrderId, orderIds);
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            for (OrderItem item : items) {
                Long pid = item.getProductId();
                int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                productSalesCount.merge(pid, qty, (a, b) -> Integer.sum(a.intValue(), b.intValue()));
                productSalesAmount.merge(pid, item.getTotalAmount(), BigDecimal::add);
            }
        }

        List<Map<String, Object>> topProducts = new ArrayList<>();
        productSalesCount.entrySet().stream()
            .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
            .limit(10)
            .forEach(entry -> {
                Product p = productMapper.selectById(entry.getKey());
                if (p != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", p.getName());
                    map.put("salesCount", entry.getValue());
                    map.put("salesAmount", productSalesAmount.getOrDefault(entry.getKey(), BigDecimal.ZERO));
                    topProducts.add(map);
                }
            });
        result.put("topProducts", topProducts);

        Map<Integer, Long> orderStats = new HashMap<>();
        if (!orderIds.isEmpty()) {
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.in(Order::getId, orderIds).eq(Order::getIsDelete, 0);
            List<Order> orders = orderMapper.selectList(orderWrapper);
            orderStats = orders.stream()
                .filter(o -> o.getStatus() != null)
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        }
        result.put("orderStats", orderStats);

        return Result.success(result);
    }
}
