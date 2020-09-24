package com.leyou.service;

import com.leyou.item.pojo.Category;

import java.util.List;

public interface CategoryService {

    //public void find();

    List<Category> queryCategoriesByPid(Long pid);

    List<String> queryCategoriesNamesByIds(List<Long> ids);
}
