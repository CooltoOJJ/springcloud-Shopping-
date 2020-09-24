package com.leyou.common.pojo;

import java.util.List;

public class PageResult<T> {

    private Long total;
    private Integer totoalPage;
    private List<T> items;

    public PageResult(Long total,List<T> items){
        this.total = total;
        this.items = items;
    }

    public PageResult(Long total,List<T> items,Integer totoalPage){
        this.total = total;
        this.items = items;
        this.totoalPage = totoalPage;
    }

    public PageResult() {
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getTotoalPage() {
        return totoalPage;
    }

    public void setTotoalPage(Integer totoalPage) {
        this.totoalPage = totoalPage;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
