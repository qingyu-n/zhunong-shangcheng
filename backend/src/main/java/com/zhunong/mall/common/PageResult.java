package com.zhunong.mall.common;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果封装类
 * @param <T> 数据类型
 */
public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页数据
     */
    private List<T> list;
    
    /**
     * 当前页码
     */
    private Long pageNum;
    
    /**
     * 每页大小
     */
    private Long pageSize;
    
    /**
     * 总页数
     */
    private Long pages;
    
    /**
     * 是否有下一页
     */
    private Boolean hasNextPage;
    
    /**
     * 是否有上一页
     */
    private Boolean hasPreviousPage;
    
    public PageResult() {
    }
    
    public PageResult(Long total, List<T> list, Long pageNum, Long pageSize) {
        this.total = total;
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (total + pageSize - 1) / pageSize;
        this.hasNextPage = pageNum < pages;
        this.hasPreviousPage = pageNum > 1;
    }
    
    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Long getPageNum() {
        return pageNum;
    }

    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public Long getPages() {
        return pages;
    }

    public void setPages(Long pages) {
        this.pages = pages;
    }

    public Boolean getHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(Boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public Boolean getHasPreviousPage() {
        return hasPreviousPage;
    }

    public void setHasPreviousPage(Boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> build(Long total, List<T> list, Long pageNum, Long pageSize) {
        return new PageResult<>(total, list, pageNum, pageSize);
    }
    
    /**
     * 构建分页结果（简化版）
     */
    public static <T> PageResult<T> of(List<T> list, Long total) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total != null ? total : (long) (list != null ? list.size() : 0));
        result.setPageNum(1L);
        result.setPageSize((long) (list != null ? list.size() : 0));
        result.setPages(total != null && total > 0 && list != null && list.size() > 0 ? (total + list.size() - 1) / list.size() : 1L);
        result.setHasNextPage(total != null && list != null && total > list.size());
        result.setHasPreviousPage(false);
        return result;
    }

    public static <T> PageResult<T> of(List<T> list, Long total, Long pageNum, Long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total != null ? total : (long) (list != null ? list.size() : 0));
        result.setPageNum(pageNum != null ? pageNum : 1L);
        result.setPageSize(pageSize != null ? pageSize : (long) (list != null ? list.size() : 0));
        Long ps = pageSize != null && pageSize > 0 ? pageSize : 10L;
        result.setPages(total != null ? (total + ps - 1) / ps : 1L);
        result.setHasNextPage(pageNum != null && result.getPages() > pageNum);
        result.setHasPreviousPage(pageNum != null && pageNum > 1);
        return result;
    }
}
