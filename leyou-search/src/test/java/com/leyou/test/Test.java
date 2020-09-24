package com.leyou.test;


import com.leyou.LeyouSearchApplication;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.Spubo;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.pojo.Goods;
import com.leyou.reponsitory.GoodsReponsitory;
import com.leyou.service.GoodsService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = LeyouSearchApplication.class)
@RunWith(SpringRunner.class)
public class Test {

    @Autowired
    CategoryClient categoryClient;

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    GoodsReponsitory goodsReponsitory;

    @Autowired
    GoodsService goodsService;

    @Autowired
    GoodsClient goodsClient;

    @org.junit.Test
    public void testCategory(){

     //   List<String> strings = categoryClient.queryNamesByIds(Arrays.asList(1L, 2L, 3L));
      //  PageResult<Spubo> spubos = goodsClient.querySpu(null, true, 1, 10);
        SpuDetail spuDetail = goodsClient.querySpudetailById(2l);
        // List<Sku> skus = goodsClient.querySkusBySpuId(2l);

//        strings.forEach(s -> {
//            System.out.println(s);
//        });
    }

    @org.junit.Test
    public void testImportData(){

        elasticsearchTemplate.createIndex(Goods.class);//创建索引
        elasticsearchTemplate.putMapping(Goods.class);//创建映射字段

        Integer page = 1;
        Integer row = 10;


        do {
            PageResult<Spubo> spubos = goodsClient.querySpu(null, true, page, row);
          System.out.println("123");
            List<Spubo> items = spubos.getItems();


            List<Goods>goodsList = items.stream().map(item -> {
                System.out.println("45");
               // Goods goods = new Goods();
                try {
                   return goodsService.buildGoods(item);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }).collect(Collectors.toList());
        System.out.println("5");
            goodsReponsitory.saveAll(goodsList);

            page++;
            row = spubos.getItems().size();


        } while (row==10);





    }
}
