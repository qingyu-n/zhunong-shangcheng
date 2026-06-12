package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhunong.mall.common.BaseEntity;

/**
 * 轮播图实体类 - 对应 carousel 表
 */
@TableName("carousel")
public class Banner extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String title;
    
    private String subtitle;
    
    @TableField("image")
    private String imageUrl;
    
    @TableField("link")
    private String linkUrl;
    
    @TableField("sort_order")
    private Integer sort;
    
    private Integer status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
