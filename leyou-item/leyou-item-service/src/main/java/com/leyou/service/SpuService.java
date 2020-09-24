package com.leyou.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.Spubo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;

import java.util.List;

public interface SpuService {
    PageResult<Spubo> querySpu(String key, Boolean saleable, Integer page, Integer rows);

    void saveGoods(Spubo spubo);

    SpuDetail querySpudetailById(Long spuid);

    List<Sku> querySkusBySupId(Long id);

    void updateGoods(Spubo spubo);

    Spu querySpuById(Long id);

    Sku querySkuById(Long id);
}
