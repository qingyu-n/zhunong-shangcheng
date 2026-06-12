package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhunong.mall.common.PageResult;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.Product;
import com.zhunong.mall.entity.ProductDetail;
import com.zhunong.mall.entity.SpecialProduct;
import com.zhunong.mall.entity.Category;
import com.zhunong.mall.mapper.ProductDetailMapper;
import com.zhunong.mall.mapper.ProductMapper;
import com.zhunong.mall.mapper.SpecialProductMapper;
import com.zhunong.mall.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SpecialProductMapper specialProductMapper;

    @Autowired
    private ProductDetailMapper productDetailMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            ensureSpecialProductTable();
            ensureProductDetailTable();
            ensureProductColumns();
        } catch (Exception e) {
            System.err.println("初始化表结构失败: " + e.getMessage());
        }
    }

    private void ensureSpecialProductTable() {
        try {
            jdbcTemplate.execute("SELECT 1 FROM `special_product` LIMIT 1");
            try {
                jdbcTemplate.execute("ALTER TABLE `special_product` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT DEFAULT 0");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("UPDATE `special_product` SET `is_delete` = COALESCE(`is_delete`, 0) WHERE `is_delete` IS NULL");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("ALTER TABLE `special_product` ADD COLUMN IF NOT EXISTS `type` VARCHAR(50) NOT NULL DEFAULT 'special_offer'");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("ALTER TABLE `special_product` ADD COLUMN IF NOT EXISTS `discount_percent` INT DEFAULT NULL");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("ALTER TABLE `special_product` ADD COLUMN IF NOT EXISTS `description` TEXT");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("ALTER TABLE `special_product` ADD COLUMN IF NOT EXISTS `image` VARCHAR(500) DEFAULT NULL");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("ALTER TABLE `special_product` ADD COLUMN IF NOT EXISTS `sort_order` INT DEFAULT 0");
            } catch (Exception ignored) {}
        } catch (Exception e) {
            try {
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
                    "PRIMARY KEY (`id`)," +
                    "INDEX `idx_type` (`type`)," +
                    "INDEX `idx_product_id` (`product_id`)," +
                    "INDEX `idx_status` (`status`)," +
                    "INDEX `idx_time_range` (`start_time`, `end_time`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='特殊商品管理表'");
                System.out.println("special_product表创建成功");
            } catch (Exception ex) {
                System.err.println("创建special_product表失败: " + ex.getMessage());
            }
        }
    }

    private void ensureProductDetailTable() {
        try {
            jdbcTemplate.execute("SELECT 1 FROM `product_detail` LIMIT 1");
            // 表已存在，检查并添加缺失的列
            try {
                jdbcTemplate.execute("ALTER TABLE `product_detail` ADD COLUMN IF NOT EXISTS `farmer_story` TEXT");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("ALTER TABLE `product_detail` ADD COLUMN IF NOT EXISTS `farmer_image` VARCHAR(500)");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("ALTER TABLE `product_detail` ADD COLUMN IF NOT EXISTS `param_name` VARCHAR(200)");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("ALTER TABLE `product_detail` ADD COLUMN IF NOT EXISTS `param_origin` VARCHAR(100)");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("ALTER TABLE `product_detail` ADD COLUMN IF NOT EXISTS `param_shelf_life` VARCHAR(50)");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("ALTER TABLE `product_detail` ADD COLUMN IF NOT EXISTS `param_storage` VARCHAR(200)");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("ALTER TABLE `product_detail` ADD COLUMN IF NOT EXISTS `param_delivery` VARCHAR(100)");
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.execute("ALTER TABLE `product_detail` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT DEFAULT 0");
            } catch (Exception ignored) {}
        } catch (Exception e) {
            try {
                jdbcTemplate.execute("CREATE TABLE `product_detail` (" +
                    "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                    "`product_id` BIGINT NOT NULL," +
                    "`description` TEXT," +
                    "`farmer_story` TEXT," +
                    "`farmer_image` VARCHAR(500)," +
                    "`image1` VARCHAR(500)," +
                    "`image2` VARCHAR(500)," +
                    "`image3` VARCHAR(500)," +
                    "`image4` VARCHAR(500)," +
                    "`image5` VARCHAR(500)," +
                    "`param_name` VARCHAR(200)," +
                    "`param_origin` VARCHAR(100)," +
                    "`param_shelf_life` VARCHAR(50)," +
                    "`param_storage` VARCHAR(200)," +
                    "`param_delivery` VARCHAR(100)," +
                    "`status` TINYINT DEFAULT 1," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "`is_delete` TINYINT DEFAULT 0," +
                    "PRIMARY KEY (`id`)," +
                    "INDEX `idx_product_id` (`product_id`)," +
                    "INDEX `idx_status` (`status`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品详情表'");
                System.out.println("product_detail表创建成功");
            } catch (Exception ex) {
                System.err.println("创建product_detail表失败: " + ex.getMessage());
            }
        }
    }

    private void ensureProductColumns() {
        try {
            jdbcTemplate.execute("ALTER TABLE `product` ADD COLUMN IF NOT EXISTS `is_hot` TINYINT DEFAULT 0");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE `product` ADD COLUMN IF NOT EXISTS `is_help` TINYINT DEFAULT 0");
        } catch (Exception ignored) {}
    }

    @GetMapping("/list")
    public Result<PageResult<Product>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        try {
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getStatus, 1);

            if (categoryId != null && !categoryId.isEmpty()) {
                try {
                    Long id = Long.parseLong(categoryId);
                    List<Long> categoryIds = new java.util.ArrayList<>();
                    categoryIds.add(id);
                    List<Category> childCategories = categoryMapper.selectList(
                            new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
                    for (Category child : childCategories) {
                        categoryIds.add(child.getId());
                    }
                    wrapper.in(Product::getCategoryId, categoryIds);
                } catch (NumberFormatException e) {
                    LambdaQueryWrapper<Category> catWrapper = new LambdaQueryWrapper<>();
                    catWrapper.like(Category::getName, categoryId);
                    Category category = categoryMapper.selectOne(catWrapper);
                    if (category != null) {
                        List<Long> categoryIds = new java.util.ArrayList<>();
                        categoryIds.add(category.getId());
                        List<Category> childCategories = categoryMapper.selectList(
                                new LambdaQueryWrapper<Category>().eq(Category::getParentId, category.getId()));
                        for (Category child : childCategories) {
                            categoryIds.add(child.getId());
                        }
                        wrapper.in(Product::getCategoryId, categoryIds);
                    }
                }
            }

            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like(Product::getName, keyword);
            }

            if ("price_asc".equals(sort) || "price-asc".equals(sort)) {
                wrapper.orderByAsc(Product::getPrice);
            } else if ("price_desc".equals(sort) || "price-desc".equals(sort)) {
                wrapper.orderByDesc(Product::getPrice);
            } else if ("sales".equals(sort)) {
                wrapper.orderByDesc(Product::getSales);
            } else {
                wrapper.orderByDesc(Product::getSortOrder);
            }

            // 价格区间筛选
            if (minPrice != null) {
                wrapper.ge(Product::getPrice, minPrice);
            }
            if (maxPrice != null) {
                wrapper.le(Product::getPrice, maxPrice);
            }

            Page<Product> pageParam = new Page<>(page, size);
            Page<Product> result = productMapper.selectPage(pageParam, wrapper);

            return Result.success(PageResult.of(result.getRecords(), result.getTotal()));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取商品列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
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

    @GetMapping("/search")
    public Result<PageResult<Product>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getStatus, 1);
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or()
                    .like(Product::getDescription, keyword));
            wrapper.orderByDesc(Product::getSortOrder);

            Page<Product> pageParam = new Page<>(page, size);
            Page<Product> result = productMapper.selectPage(pageParam, wrapper);

            return Result.success(PageResult.of(result.getRecords(), result.getTotal()));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "搜索商品失败: " + e.getMessage());
        }
    }

    @GetMapping("/hot")
    public Result<PageResult<Product>> getHotProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getStatus, 1);
            wrapper.eq(Product::getIsHot, 1);
            wrapper.orderByDesc(Product::getSales);
            Page<Product> pageParam = new Page<>(page, size);
            Page<Product> result = productMapper.selectPage(pageParam, wrapper);
            return Result.success(PageResult.of(result.getRecords(), result.getTotal()));
        } catch (Exception e) {
            return Result.error(500, "获取热销商品失败: " + e.getMessage());
        }
    }

    @GetMapping("/premium")
    public Result<PageResult<Product>> getPremiumProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getStatus, 1);
            wrapper.eq(Product::getIsHelp, 1);
            wrapper.orderByDesc(Product::getCreateTime);
            Page<Product> pageParam = new Page<>(page, size);
            Page<Product> result = productMapper.selectPage(pageParam, wrapper);
            return Result.success(PageResult.of(result.getRecords(), result.getTotal()));
        } catch (Exception e) {
            return Result.error(500, "获取助农优选商品失败: " + e.getMessage());
        }
    }

    @GetMapping("/special-offers")
    public Result<List<Map<String, Object>>> getSpecialOffers() {
        try {
            LambdaQueryWrapper<SpecialProduct> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SpecialProduct::getType, "special_offer");
            wrapper.eq(SpecialProduct::getStatus, 1);
            wrapper.le(SpecialProduct::getStartTime, LocalDateTime.now());
            wrapper.ge(SpecialProduct::getEndTime, LocalDateTime.now());
            wrapper.orderByAsc(SpecialProduct::getSortOrder);
            List<SpecialProduct> list = specialProductMapper.selectList(wrapper);

            List<Map<String, Object>> result = new ArrayList<>();
            for (SpecialProduct sp : list) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", sp.getId());
                item.put("productId", sp.getProductId());
                item.put("name", sp.getName());
                item.put("image", sp.getImage());
                item.put("originalPrice", sp.getOriginalPrice());
                item.put("discountPrice", sp.getDiscountPrice());
                item.put("discountPercent", sp.getDiscountPercent());
                item.put("startTime", sp.getStartTime());
                item.put("endTime", sp.getEndTime());
                item.put("description", sp.getDescription());

                if (sp.getProductId() != null) {
                    try {
                        Product p = productMapper.selectById(sp.getProductId());
                        if (p != null) {
                            if (sp.getName() == null || sp.getName().isEmpty()) {
                                item.put("name", p.getName());
                            }
                            if (sp.getImage() == null || sp.getImage().isEmpty()) {
                                item.put("image", p.getMainImage());
                            }
                            if (sp.getOriginalPrice() == null) {
                                item.put("originalPrice", p.getPrice());
                            }
                        }
                    } catch (Exception ignored) {}
                }
                result.add(item);
            }
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取限时特惠商品失败: " + e.getMessage());
        }
    }

    // 获取商品详情信息（前端商品详情页使用）
    @GetMapping("/detail/product/{productId}")
    public Result<Map<String, Object>> getProductDetailInfo(@PathVariable Long productId) {
        try {
            ProductDetail detail = productDetailMapper.selectOne(
                new LambdaQueryWrapper<ProductDetail>()
                    .eq(ProductDetail::getProductId, productId)
                    .eq(ProductDetail::getIsDelete, 0)
                    .last("LIMIT 1")
            );
            
            // 获取商品信息
            Product product = productMapper.selectById(productId);
            
            Map<String, Object> result = new HashMap<>();
            
            if (detail != null) {
                result.put("id", detail.getId());
                result.put("productId", detail.getProductId());
                result.put("description", detail.getDescription());
                result.put("farmerStory", detail.getFarmerStory());
                result.put("farmerImage", detail.getFarmerImage());
                
                // 图片列表
                List<String> images = new ArrayList<>();
                if (detail.getImage1() != null && !detail.getImage1().isEmpty()) images.add(detail.getImage1());
                if (detail.getImage2() != null && !detail.getImage2().isEmpty()) images.add(detail.getImage2());
                if (detail.getImage3() != null && !detail.getImage3().isEmpty()) images.add(detail.getImage3());
                if (detail.getImage4() != null && !detail.getImage4().isEmpty()) images.add(detail.getImage4());
                if (detail.getImage5() != null && !detail.getImage5().isEmpty()) images.add(detail.getImage5());
                result.put("images", images);
                
                // 参数列表
                List<Map<String, String>> params = new ArrayList<>();
                params.add(Map.of("label", "商品名称", "value", detail.getParamName() != null ? detail.getParamName() : (product != null ? product.getName() : "")));
                params.add(Map.of("label", "产地", "value", detail.getParamOrigin() != null ? detail.getParamOrigin() : ""));
                params.add(Map.of("label", "保质期", "value", detail.getParamShelfLife() != null ? detail.getParamShelfLife() : ""));
                params.add(Map.of("label", "储存方式", "value", detail.getParamStorage() != null ? detail.getParamStorage() : ""));
                params.add(Map.of("label", "发货时间", "value", detail.getParamDelivery() != null ? detail.getParamDelivery() : ""));
                result.put("params", params);
            } else {
                // 没有详情数据时返回默认值
                result.put("id", null);
                result.put("productId", productId);
                result.put("description", "");
                result.put("farmerStory", "");
                result.put("farmerImage", product != null ? product.getMainImage() : "");
                result.put("images", product != null && product.getMainImage() != null ? List.of(product.getMainImage()) : List.of());
                
                List<Map<String, String>> params = new ArrayList<>();
                params.add(Map.of("label", "商品名称", "value", product != null ? product.getName() : ""));
                params.add(Map.of("label", "产地", "value", ""));
                params.add(Map.of("label", "保质期", "value", ""));
                params.add(Map.of("label", "储存方式", "value", ""));
                params.add(Map.of("label", "发货时间", "value", ""));
                result.put("params", params);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取商品详情信息失败: " + e.getMessage());
        }
    }
}
