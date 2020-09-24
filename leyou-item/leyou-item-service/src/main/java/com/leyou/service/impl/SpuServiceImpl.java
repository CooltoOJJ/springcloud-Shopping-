package com.leyou.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.Spubo;
import com.leyou.item.pojo.*;
import com.leyou.mapper.*;
import com.leyou.service.CategoryService;
import com.leyou.service.SpuService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageResult<Spubo> querySpu(String key, Boolean saleable, Integer page, Integer rows) {

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //添加搜索字段
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }

        //添加上架下架搜索字段
        if(saleable != null){
            criteria.andEqualTo("saleable",saleable);
        }

        //分页查询
        PageHelper.startPage(page,rows);

        //查询数据库
        List<Spu> spuList = this.spuMapper.selectByExample(example);
        PageInfo<Spu> spuPageInfo = new PageInfo<>(spuList);

        //封装数据Sup 成SupDetail
        List<Spubo> spubos = spuList.stream().map(spu ->{
            Spubo spubo = new Spubo();
            BeanUtils.copyProperties(spu,spubo);

            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());

            List<String> categoriesName= categoryService.queryCategoriesNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

            spubo.setBname(brand.getName());
            spubo.setCname(StringUtils.join(categoriesName,"-"));

            return spubo;
        }).collect(Collectors.toList());


        return new PageResult<>(spuPageInfo.getTotal(),spubos);
    }

    /**
     * 新增商品
     * @param spubo
     */

    @Override
    @Transactional
    public void saveGoods(Spubo spubo) {

        //先新增spu
        spubo.setId(null);
        spubo.setSaleable(true);
        spubo.setValid(true);
        spubo.setCreateTime(new Date());
        spubo.setLastUpdateTime(spubo.getCreateTime());
        this.spuMapper.insertSelective(spubo);

        //再新增spudetail
        SpuDetail spuDetail = spubo.getSpuDetail();
        spuDetail.setSpuId(spubo.getId());
        //System.out.println("查看spuid是否有返回："+spubo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);

        //新增Sku
//        List<Sku> skus = spubo.getSkus();
//        skus.forEach(sku -> {
//
//            sku.setId(null);
//            sku.setSpuId(spubo.getId());
//            sku.setCreateTime(new Date());
//            sku.setLastUpdateTime(sku.getCreateTime());
//            this.skuMapper.insertSelective(sku);
//
//
//            //最后新增stock
//            Stock stock = new Stock();
//            stock.setSkuId(sku.getId());
//            stock.setStock(sku.getStock());
//            this.stockMapper.insertSelective(stock);
//        });

        saveSkuAndStock(spubo);

        //发送插入消息到消息中间件
        this.sendToRabbitMQ("insert",spubo);

    }

    //新增sku
    //新增stock
    private void saveSkuAndStock(Spubo spubo){
        List<Sku> skus = spubo.getSkus();
        skus.forEach(sku -> {

            sku.setId(null);
            sku.setSpuId(spubo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);


            //最后新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }

    /**
     * 根据spuid 查询spudeatil
     * @param spuid
     * @return
     */
    @Override
    public SpuDetail querySpudetailById(Long spuid) {

        return this.spuDetailMapper.selectByPrimaryKey(spuid);
    }

    /**
     *根据spuid查询sku集合，并且查询stock库存
     * @param id
     * @return
     */
    @Override
    public List<Sku> querySkusBySupId(Long id) {

        Sku record = new Sku();
        record.setSpuId(id);

        List<Sku> skus = this.skuMapper.select(record);

        skus.forEach(sku -> {

            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        });

        return skus;
    }

    /**
     * 更新商品信息
     * @param spubo
     */

    @Transactional
    public void updateGoods(Spubo spubo) {


        Sku record = new Sku();
        record.setSpuId(spubo.getId());
        List<Sku> skus = this.skuMapper.select(record);
        //删除stock表对应数据
        skus.forEach(sku -> {
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        });

        //删除sku表对应数据
        this.skuMapper.delete(record);

        //新增sku
        //新增stock
        this.saveSkuAndStock(spubo);

        //更新Spu表
        spubo.setCreateTime(null);//不能更新的东西设置为null
        spubo.setLastUpdateTime(new Date());//更新时间以系统时间为准
        spubo.setValid(null);
        spubo.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spubo);
        //更新SpuDetail表
        this.spuDetailMapper.updateByPrimaryKeySelective(spubo.getSpuDetail());

        System.out.println("更新了商品");
        //发送更新消息到rabbitMQ
        this.sendToRabbitMQ("update",spubo);

    }

    @Override
    public Spu querySpuById(Long id) {

        return this.spuMapper.selectByPrimaryKey(id);
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public Sku querySkuById(Long id) {

        return this.skuMapper.selectByPrimaryKey(id);
    }

    /**
     * 方法：发送消息到rabbitMQ,
     * @param type 指定消息的类型，路由key
     * @param spubo 消息
     */
    private void sendToRabbitMQ(String type,Spubo spubo){

        try {
            amqpTemplate.convertAndSend("item."+type,spubo.getId());
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }
}
