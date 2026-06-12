package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhunong.mall.common.BaseEntity;

/**
 * 商品分类实体类 - 对应 product_category 表
 */
@TableName("product_category")
public class Category extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String name;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
    private String icon;
    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
