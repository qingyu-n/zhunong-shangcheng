package com.zhunong.mall.controller;

import com.zhunong.mall.common.Result;
import com.zhunong.mall.common.PageResult;
import com.zhunong.mall.entity.FarmerProfile;
import com.zhunong.mall.entity.Product;
import com.zhunong.mall.entity.Order;
import com.zhunong.mall.entity.OrderItem;
import com.zhunong.mall.mapper.OrderMapper;
import com.zhunong.mall.mapper.OrderItemMapper;
import com.zhunong.mall.mapper.ProductMapper;
import com.zhunong.mall.service.FarmerService;
import com.zhunong.mall.utils.JwtUtil;
import com.zhunong.mall.vo.OrderVO;
import com.zhunong.mall.vo.OrderItemVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@RestController
@RequestMapping("/farmer")
public class FarmerController {

    @Autowired
    private FarmerService farmerService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    private Long getUserIdFromHeader(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }

    @PostMapping("/apply")
    public Result<FarmerProfile> apply(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody FarmerProfile profile) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            profile.setUserId(userId);
            if (profile.getContactName() == null && profile.getShopName() != null) {
                // 如果前端传了realName但没有contactName，用shopName作为默认值
            }
            return farmerService.applyFarmer(userId, profile);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "提交申请失败: " + e.getMessage());
        }
    }

    @GetMapping("/profile")
    public Result<FarmerProfile> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            FarmerProfile profile = farmerService.getFarmerProfileByUserId(userId);
            return Result.success(profile);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取农户资料失败: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    public Result<FarmerProfile> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody FarmerProfile profile) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return farmerService.updateFarmerProfile(userId, profile);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "更新农户资料失败: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public Result<Integer> getStatus(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return Result.success(null);
            }
            Integer status = farmerService.getApplyStatus(userId);
            return Result.success(status);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.success(null);
        }
    }

    @PostMapping("/products")
    public Result<Product> publishProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Product product) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return farmerService.publishProduct(userId, product);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "发布商品失败: " + e.getMessage());
        }
    }

    @GetMapping("/products")
    public Result<PageResult<Product>> getMyProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Integer status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return farmerService.getMyProducts(userId, page, size, keyword, auditStatus, status);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取商品列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/products/{id}")
    public Result<Product> getMyProductDetail(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return farmerService.getMyProductDetail(userId, id);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取商品详情失败: " + e.getMessage());
        }
    }

    @PutMapping("/products/{id}")
    public Result<Void> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return farmerService.updateProduct(userId, id, product);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "更新商品失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return farmerService.deleteProduct(userId, id);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "删除商品失败: " + e.getMessage());
        }
    }

    @PutMapping("/products/{id}/status")
    public Result<Void> updateProductStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return farmerService.updateProductStatus(userId, id, status);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "更新商品状态失败: " + e.getMessage());
        }
    }

    @GetMapping("/orders")
    public Result<PageResult<OrderVO>> getFarmerOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
            productWrapper.eq(Product::getFarmerId, userId).select(Product::getId);
            List<Long> productIds = productMapper.selectList(productWrapper)
                    .stream().map(Product::getId).collect(java.util.stream.Collectors.toList());
            if (productIds.isEmpty()) {
                return Result.success(PageResult.of(new ArrayList<>(), 0L));
            }
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(OrderItem::getProductId, productIds).select(OrderItem::getOrderId);
            List<Long> orderIds = orderItemMapper.selectList(itemWrapper)
                    .stream().map(OrderItem::getOrderId).distinct().collect(java.util.stream.Collectors.toList());
            if (orderIds.isEmpty()) {
                return Result.success(PageResult.of(new ArrayList<>(), 0L));
            }
            Page<Order> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.in(Order::getId, orderIds)
                       .eq(status != null, Order::getStatus, status)
                       .orderByDesc(Order::getCreateTime);
            Page<Order> result = orderMapper.selectPage(pageParam, orderWrapper);
            
            List<OrderVO> voList = new ArrayList<>();
            for (Order order : result.getRecords()) {
                OrderVO vo = convertToVO(order);
                LambdaQueryWrapper<OrderItem> itemsWrapper = new LambdaQueryWrapper<>();
                itemsWrapper.eq(OrderItem::getOrderId, order.getId());
                List<OrderItem> items = orderItemMapper.selectList(itemsWrapper);
                List<OrderItemVO> itemVOs = new ArrayList<>();
                for (OrderItem item : items) {
                    OrderItemVO itemVO = new OrderItemVO();
                    itemVO.setId(item.getId());
                    itemVO.setProductId(item.getProductId());
                    itemVO.setProductName(item.getProductName());
                    itemVO.setProductImage(item.getProductImage());
                    itemVO.setPrice(item.getProductPrice() != null ? item.getProductPrice() : item.getTotalAmount() != null ? item.getTotalAmount().divide(item.getQuantity() != null ? BigDecimal.valueOf(item.getQuantity()) : BigDecimal.ONE) : BigDecimal.ZERO);
                    itemVO.setQuantity(item.getQuantity());
                    itemVO.setTotalAmount(item.getTotalAmount());
                    itemVOs.add(itemVO);
                }
                vo.setItems(itemVOs);
                voList.add(vo);
            }
            return Result.success(PageResult.of(voList, result.getTotal()));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取农户订单失败: " + e.getMessage());
        }
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
        return vo;
    }

    @PutMapping("/orders/{id}/ship")
    public Result<Void> shipOrder(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> shipData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            Order order = orderMapper.selectById(id);
            if (order == null) {
                return Result.error(404, "订单不存在");
            }
            LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
            productWrapper.eq(Product::getFarmerId, userId).select(Product::getId);
            List<Long> productIds = productMapper.selectList(productWrapper)
                    .stream().map(Product::getId).collect(java.util.stream.Collectors.toList());
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(OrderItem::getOrderId, id).in(OrderItem::getProductId, productIds);
            Long count = orderItemMapper.selectCount(itemWrapper);
            if (count == 0) {
                return Result.error(403, "无权操作此订单");
            }
            if (order.getStatus() != 1) {
                return Result.error(400, "只有已付款订单才能发货");
            }
            order.setStatus(2);
            order.setLogisticsCompany(shipData.get("logisticsCompany"));
            order.setTrackingNumber(shipData.get("trackingNumber"));
            orderMapper.updateById(order);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "发货失败: " + e.getMessage());
        }
    }

    @PutMapping("/orders/{id}/cancel-ship")
    public Result<Void> cancelShip(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            Order order = orderMapper.selectById(id);
            if (order == null) {
                return Result.error(404, "订单不存在");
            }
            LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
            productWrapper.eq(Product::getFarmerId, userId).select(Product::getId);
            List<Long> productIds = productMapper.selectList(productWrapper)
                    .stream().map(Product::getId).collect(java.util.stream.Collectors.toList());
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(OrderItem::getOrderId, id).in(OrderItem::getProductId, productIds);
            Long count = orderItemMapper.selectCount(itemWrapper);
            if (count == 0) {
                return Result.error(403, "无权操作此订单");
            }
            if (order.getStatus() != 2) {
                return Result.error(400, "只有已发货订单才能取消发货");
            }
            order.setStatus(1);
            order.setLogisticsCompany(null);
            order.setTrackingNumber(null);
            orderMapper.updateById(order);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "取消发货失败: " + e.getMessage());
        }
    }
}
