package com.leyou.sevice;

import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.client.SpecParamClient;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecParamClient specParamClient;

    public Map<String,Object> loadData(Long id){

        Map<String,Object> modle = new HashMap<>();

        //查询spu
        Spu spu = this.goodsClient.querySpuById(id);

        //查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpudetailById(id);

        //查询分类
        List<Long> lists = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> name = this.categoryClient.queryNamesByIds(lists);
        List<Map<String,Object>> categories = new ArrayList<>();
        for (int i = 0; i <name.size(); i++) {

            Map<String,Object> category = new HashMap<>();
            category.put("id",lists.get(i));
            category.put("name",name.get(i));

            categories.add(category);
        }

        //查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        //查询sku
        List<Sku> skus = this.goodsClient.querySkusBySpuId(id);

        //查询groups
        List<SpecGroup> groups = this.specParamClient.querySpecsByCid(spu.getCid3());

        //查询特殊参数
        List<SpecParam> params = this.specParamClient.querySpecParam(null, spu.getCid3(), false, null);
        Map<Long,String> paramMap = new HashMap<>();
        params.forEach(param->{
            paramMap.put(param.getId(),param.getName());
        });

        modle.put("spu",spu);
        modle.put("spuDetail",spuDetail);
        modle.put("categories",categories);
        modle.put("brand",brand);
        modle.put("skus",skus);
        modle.put("groups",groups);
        modle.put("paramMap",paramMap);

        return modle;

    }

}
