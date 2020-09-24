package com.leyou.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.Spubo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class GoodsController {

    @Autowired
    private SpuService spuService;

    /**
     * 查看所有商品
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<Spubo>> querySpu(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows
    ){
        System.out.println("运行了querySpu");
        PageResult<Spubo> pageResult = this.spuService.querySpu(key,saleable,page,rows);

        if (pageResult==null|| CollectionUtils.isEmpty(pageResult.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 新增商品
     * @param spubo
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spubo spubo){

        this.spuService.saveGoods(spubo);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * 根据spuid 查询spudeatil
     * @param spuid
     * @return
     */
    @GetMapping("spu/detail/{spuid}")
    public ResponseEntity<SpuDetail> querSpuById(@PathVariable("spuid")Long spuid){

        SpuDetail spuDetail = this.spuService.querySpudetailById(spuid);

        if (spuDetail == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(spuDetail);
    }


    /**
     * 根据spuid查询sku集合
     * @param id
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuId(Long id){

        List<Sku> skus = this.spuService.querySkusBySupId(id);

        if (CollectionUtils.isEmpty(skus)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skus);
    }

    /**
     * 更新商品信息
     * @param spubo
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spubo spubo){

        this.spuService.updateGoods(spubo);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spuID 查询spu
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id")Long id){

        Spu spu = this.spuService.querySpuById(id);

        if(spu == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(spu);

    }

    /**
     * 根据skuid 查询sku
     * @return
     */
    @GetMapping("sku/{id}")
    public ResponseEntity<Sku> querySkuById(@PathVariable("id") Long id){

        Sku sku =this.spuService.querySkuById(id);
        if (sku == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(sku);
    }


}
