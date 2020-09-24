package com.leyou.controller;

import com.leyou.item.pojo.Category;
import com.leyou.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("category")
public class CategroyController {

    @Autowired
    CategoryService categoryService;

    /**
     * 根据父节点id查询子节点
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoriesByPid(@RequestParam(value="pid",defaultValue = "0") Long pid){


        if(pid ==null||pid<0){
            //返回400：传入参数不合法
            //return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();//3种写法优化
            //return ResponseEntity.badRequest().build();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<Category> categories = categoryService.queryCategoriesByPid(pid);
        if(CollectionUtils.isEmpty(categories)){
            //返回404：服务器资源未找到
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        //返回200：成功返回数据
        return ResponseEntity.ok(categories);

   }

    /**
     * 根据ids(数组),查询对应品牌名数组返回
     * @param ids
     * @return
     */
    @GetMapping("names")
    public ResponseEntity<List<String>> queryNamesByIds(@RequestParam("ids") List<Long> ids){

        List<String> strings = this.categoryService.queryCategoriesNamesByIds(ids);

        if(CollectionUtils.isEmpty(strings)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(strings);
    }


}
