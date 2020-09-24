package com.leyou.pojo;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;

import java.util.List;
import java.util.Map;

public class SearchResult extends PageResult<Goods> {
    //分类的集合
    private List<Map<String,Object>> categories;
    //品牌的集合
    private List<Brand> brands;
    //参数的集合
    private List<Map<String,Object>> spec;


    public SearchResult() {

    }

    public SearchResult(Long total, List<Goods> items, List<Map<String, Object>> categories, List<Brand> brands, List<Map<String, Object>> spec) {
        super(total, items);
        this.categories = categories;
        this.brands = brands;
        this.spec = spec;
    }

    public SearchResult(Long total, List<Goods> items, Integer totoalPage, List<Map<String, Object>> categories, List<Brand> brands, List<Map<String, Object>> spec) {
        super(total, items, totoalPage);
        this.categories = categories;
        this.brands = brands;
        this.spec = spec;
    }

    public SearchResult(List<Map<String, Object>> categories, List<Brand> brands, List<Map<String, Object>> spec) {
        this.categories = categories;
        this.brands = brands;
        this.spec = spec;
    }

    public List<Map<String, Object>> getSpec() {
        return spec;
    }

    public void setSpec(List<Map<String, Object>> spec) {
        this.spec = spec;
    }

    public List<Map<String, Object>> getCategories() {
        return categories;
    }

    public void setCategories(List<Map<String, Object>> categories) {
        this.categories = categories;
    }

    public List<Brand> getBrands() {
        return brands;
    }

    public void setBrands(List<Brand> brands) {
        this.brands = brands;
    }
}
