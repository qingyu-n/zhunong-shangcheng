package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhunong.mall.common.PageResult;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.*;
import com.zhunong.mall.mapper.*;
import com.zhunong.mall.utils.JwtUtil;
import com.zhunong.mall.vo.OrderItemVO;
import com.zhunong.mall.vo.OrderStatsVO;
import com.zhunong.mall.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    @Transactional
    public Result<Order> createOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> orderData) {

        try {
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
            if (orderData.get("addressId") == null) {
                return Result.error(400, "请选择收货地址");
            }
            Long addressId = Long.valueOf(orderData.get("addressId").toString());
            String remark = orderData.get("remark") != null ? orderData.get("remark").toString() : "";

            if (items == null || items.isEmpty()) {
                return Result.error(400, "订单商品不能为空");
            }

            // 获取收货地址
            Address address = addressMapper.selectById(addressId);
            if (address == null) {
                return Result.error(404, "收货地址不存在");
            }

            // 计算订单金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (Map<String, Object> item : items) {
                Long productId = Long.valueOf(item.get("productId").toString());
                Integer quantity = Integer.valueOf(item.get("quantity").toString());

                Product product = productMapper.selectById(productId);
                if (product == null || product.getStatus() == 0) {
                    return Result.error(404, "商品不存在或已下架: " + (product != null ? product.getName() : "未知"));
                }

                int currentStock = product.getStock() != null ? product.getStock() : 0;
                if (currentStock < quantity) {
                    return Result.error(400, "商品库存不足: " + product.getName());
                }

                BigDecimal itemAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                totalAmount = totalAmount.add(itemAmount);

                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(productId);
                orderItem.setProductName(product.getName());
                orderItem.setProductImage(product.getMainImage());
                orderItem.setProductPrice(product.getPrice());
                orderItem.setQuantity(quantity);
                orderItem.setTotalAmount(itemAmount);
                orderItems.add(orderItem);

                // 扣减库存
                product.setStock(currentStock - quantity);
                int currentSales = product.getSales() != null ? product.getSales() : 0;
                product.setSales(currentSales + quantity);
                productMapper.updateById(product);
            }

            // 创建订单
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setTotalAmount(totalAmount);
            order.setFreightAmount(BigDecimal.ZERO);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setPayAmount(totalAmount);
            order.setStatus(0); // 待付款

            // 安全地设置收货人信息
            order.setReceiverName(address.getName() != null ? address.getName() : "");
            order.setReceiverPhone(address.getPhone() != null ? address.getPhone() : "");

            StringBuilder addressBuilder = new StringBuilder();
            if (address.getProvince() != null) addressBuilder.append(address.getProvince());
            if (address.getCity() != null) addressBuilder.append(address.getCity());
            if (address.getDistrict() != null) addressBuilder.append(address.getDistrict());
            if (address.getDetailAddress() != null) addressBuilder.append(address.getDetailAddress());
            order.setReceiverAddress(addressBuilder.toString());

            order.setRemark(remark);

            orderMapper.insert(order);

            // 保存订单项
            for (OrderItem item : orderItems) {
                item.setOrderId(order.getId());
                orderItemMapper.insert(item);
            }

            // 从购物车删除已下单商品
            for (Map<String, Object> item : items) {
                Long productId = Long.valueOf(item.get("productId").toString());
                LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Cart::getUserId, userId);
                wrapper.eq(Cart::getProductId, productId);
                Cart cart = cartMapper.selectOne(wrapper);
                if (cart != null) {
                    cart.setIsDelete(1);
                    cartMapper.updateById(cart);
                }
            }

            return Result.success(order);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> getOrderList(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        wrapper.eq(Order::getIsDelete, 0);
        
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        
        wrapper.orderByDesc(Order::getCreateTime);
        
        Page<Order> pageParam = new Page<>(page, size);
        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);
        
        List<OrderVO> voList = new ArrayList<>();
        for (Order order : result.getRecords()) {
            OrderVO vo = convertToVO(order);
            voList.add(vo);
        }
        
        return Result.success(PageResult.of(voList, result.getTotal()));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        Order order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error(404, "订单不存在");
        }

        return Result.success(convertToVO(order));
    }

    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    @Transactional
    public Result<Void> cancelOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        Order order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error(404, "订单不存在");
        }

        if (order.getStatus() != 0) {
            return Result.error(400, "只能取消待付款订单");
        }

        // 恢复库存
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, id);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(product.getSales() - item.getQuantity());
                productMapper.updateById(product);
            }
        }

        order.setStatus(4); // 已取消
        orderMapper.updateById(order);
        
        return Result.success();
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/stats")
    public Result<OrderStatsVO> getOrderStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        OrderStatsVO stats = new OrderStatsVO();
        
        // 待付款
        LambdaQueryWrapper<Order> wrapper0 = new LambdaQueryWrapper<>();
        wrapper0.eq(Order::getUserId, userId);
        wrapper0.eq(Order::getStatus, 0);
        wrapper0.eq(Order::getIsDelete, 0);
        stats.setUnpaidCount(orderMapper.selectCount(wrapper0));
        
        // 待发货
        LambdaQueryWrapper<Order> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(Order::getUserId, userId);
        wrapper1.eq(Order::getStatus, 1);
        wrapper1.eq(Order::getIsDelete, 0);
        stats.setUnshippedCount(orderMapper.selectCount(wrapper1));
        
        // 待收货
        LambdaQueryWrapper<Order> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(Order::getUserId, userId);
        wrapper2.eq(Order::getStatus, 2);
        wrapper2.eq(Order::getIsDelete, 0);
        stats.setUnreceivedCount(orderMapper.selectCount(wrapper2));
        
        // 已完成
        LambdaQueryWrapper<Order> wrapper3 = new LambdaQueryWrapper<>();
        wrapper3.eq(Order::getUserId, userId);
        wrapper3.eq(Order::getStatus, 3);
        wrapper3.eq(Order::getIsDelete, 0);
        stats.setCompletedCount(orderMapper.selectCount(wrapper3));
        
        return Result.success(stats);
    }

    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setStatus(order.getStatus());
        vo.setPayType(order.getPayType());
        vo.setPayTime(order.getPayTime());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setRemark(order.getRemark());
        vo.setLogisticsCompany(order.getLogisticsCompany());
        vo.setTrackingNumber(order.getTrackingNumber());
        vo.setCreateTime(order.getCreateTime());

        // 获取订单项
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        
        List<OrderItemVO> itemVOs = new ArrayList<>();
        for (OrderItem item : items) {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setProductId(item.getProductId());
            itemVO.setProductName(item.getProductName());
            itemVO.setProductImage(item.getProductImage());
            itemVO.setPrice(item.getProductPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setTotalAmount(item.getTotalAmount());
            itemVOs.add(itemVO);
        }
        vo.setItems(itemVOs);
        
        return vo;
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "ZN" + date + random;
    }

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        try {
            String token = authHeader.replace("Bearer ", "");
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}
