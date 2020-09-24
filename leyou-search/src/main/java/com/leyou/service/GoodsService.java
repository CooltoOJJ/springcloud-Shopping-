package com.leyou.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecParamClient;
import com.leyou.item.pojo.*;
import com.leyou.pojo.Goods;
import com.leyou.pojo.SearchRequest;
import com.leyou.pojo.SearchResult;
import com.leyou.reponsitory.GoodsReponsitory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    GoodsClient goodsClient;

    @Autowired
    BrandClient brandClient;

    @Autowired
    CategoryClient categoryClient;

    @Autowired
    SpecParamClient specParamClient;

    @Autowired
    GoodsReponsitory goodsReponsitory;


    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Goods buildGoods(Spu spu) throws IOException {

        Goods goods = new Goods();

        //设置sku的值
        System.out.println(spu.getId());
        List<Sku> skus = goodsClient.querySkusBySpuId(spu.getId());
        System.out.println("设置sku的值");
        List<Long> prices = new ArrayList<>();
        List<Map<String,Object>> skusMap = new ArrayList<>();
        skus.forEach(sku -> {//拿出sku必要的字段放入map
            prices.add(sku.getPrice());

            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("id",sku.getId());
            skuMap.put("title",sku.getTitle());
            skuMap.put("price",sku.getPrice());
            skuMap.put("image", StringUtils.isNotBlank(sku.getImages())
                    ? StringUtils.split(sku.getImages(),",")[0] : "");
            skusMap.add(skuMap);
        });

        //查询品牌
        Brand brands = brandClient.queryBrandById(spu.getBrandId());
        System.out.println("查询品牌");
        //查询分类名称
        List<String> categories = categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        System.out.println("查询分类名称");
        //规格查询
        List<SpecParam> specParams = specParamClient.querySpecParam(null, spu.getCid3(), null, true);
        System.out.println("规格查询:"+specParams.get(1).getName());
        //查询spuDetail
        SpuDetail spuDetail = goodsClient.querySpudetailById(spu.getId());
        System.out.println("查询spuDetail:"+spuDetail.getSpecialSpec());
        //获取通用规格参数
        Map<Long,Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<Long, Object>>() {
        });
        //获取特殊规格参数
        Map<Long,List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(),new TypeReference<Map<Long,List<Object>>>(){});

        System.out.println("定义map");
        // 定义map接收{规格参数名，规格参数值}
        Map<String, Object> paramMap = new HashMap<>();
        specParams.forEach(specParam -> {
            System.out.println("specParam:"+specParam.getName());
            if (specParam.getGeneric()){
                System.out.println("oo");
                String value = genericSpecMap.get(specParam.getId()).toString();
                System.out.println("xx:"+value);
                if(specParam.getNumeric()){
                    value= chooseSegment(value,specParam);
                    //System.out.println("jj:"+value);
                }
                // 把参数名和值放入结果集中
                paramMap.put(specParam.getName(), value);

            }else {
                System.out.println("mm");
                paramMap.put(specParam.getName(), specialSpecMap.get(specParam.getId()).toString());

            }

        });

        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setBrandId(spu.getBrandId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setPrice(prices);
        goods.setSkus(MAPPER.writeValueAsString(skusMap));
        goods.setAll(spu.getTitle() + " " + brands.getName() + " " + StringUtils.join(categories," "));

        goods.setSpecs(paramMap);
        System.out.println("执行成功返回goods");
        return goods;

    }

    private String chooseSegment(String value, SpecParam p) {

        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;

    }

    /**
     * 根据字段查询分页结果集
     * @param request
     * @return
     */
    public SearchResult search(SearchRequest request) {
        //自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加字段模糊查询 + 条件过滤
        //QueryBuilder qbuilder = QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND);
        BoolQueryBuilder qbuilder = buildBoolQueryBuilder(request);//bool组合查询,即筛选条件
        queryBuilder.withQuery(qbuilder);
        //过滤返回字段信息
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));
        //设置聚合名称
        String categoryAggs = "categoryAggs";
        String brandAggs = "brandAggs";
        //根据分类聚合查询
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggs).field("cid3"));
        //根据品牌ID聚合查询
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggs).field("brandId"));

        //分页
        Integer page = request.getPage();
        Integer size = request.getSize();
        queryBuilder.withPageable(PageRequest.of(page-1,size));
        //查询结果集，并且转换可以获得聚合的类型AggregatedPage<Goods>
        AggregatedPage<Goods> search = (AggregatedPage<Goods>)this.goodsReponsitory.search(queryBuilder.build());
        //根据分类聚合名称获取分类的聚合
        Aggregation categoryaggregation = search.getAggregation(categoryAggs);
        //根据品牌聚合名称获取品牌的聚合
        Aggregation brandaggregation = search.getAggregation(brandAggs);
        //获取分类聚合内，桶的值
        List<Map<String,Object>> categories = this.getCategoriesResult(categoryaggregation);
        //获取品牌聚合内，桶的值
        List<Brand> brands = this.getBrandsResult(brandaggregation);
        //装载参数的结果集
        List<Map<String,Object>> specAggs = new ArrayList<>();
        if(categories.size()==1){//如果分类只有一种才进行参数聚合分类
            specAggs = this.getSpecAggregation((Long)categories.get(0).get("id"),qbuilder);
        }


        return new SearchResult(search.getTotalElements(), search.getContent(),search.getTotalPages(),categories,brands,specAggs);
    }

    /**
     * 组合查询
     * @param request
     * @return
     */
    private BoolQueryBuilder buildBoolQueryBuilder(SearchRequest request) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //添加基本查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
        //获得查询条件
        Map<String, Object> filters = request.getFilter();
        //添加过滤查询
        for (Map.Entry<String,Object> filter : filters.entrySet()){

            String key = filter.getKey();
            if(StringUtils.equals("品牌",key)){
                key = "brandId";
            }else if (StringUtils.equals("分类",key)){
                key = "cid3";
            }else {
                key = "specs."+ key + ".keyword";
            }

            boolQueryBuilder.filter(QueryBuilders.termQuery(key,filter.getValue()));
        }


        return boolQueryBuilder;
    }

    /**
     * 查询参数聚合结果集
     * @param cid
     * @param qbuilder
     * @return
     */
    private List<Map<String, Object>> getSpecAggregation(Long cid, QueryBuilder qbuilder) {

        //自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加查询条件
        queryBuilder.withQuery(qbuilder);
        //参数聚合查询
        //1.查询要聚合的参数名
        List<SpecParam> params = specParamClient.querySpecParam(null, cid, null, true);
        //2.添加每个参数的聚合查询
        params.forEach(param->{

            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs."+param.getName()+".keyword"));

        });
        //过滤结果集
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        //查询结果集
        AggregatedPage<Goods> searchs = (AggregatedPage<Goods>)this.goodsReponsitory.search(queryBuilder.build());
        //解析结果集
        Aggregations aggregations = searchs.getAggregations();
        //把集合结果集转换成Map数据结构
        Map<String, Aggregation> aggregationMap = aggregations.asMap();

        List<Map<String,Object>> result = new ArrayList<>();
        //遍历Map.Entry
        for (Map.Entry<String,Aggregation> entry : aggregationMap.entrySet()){

            Map<String,Object> spec = new HashMap<>();

            String key = entry.getKey();//获得聚合名,即参数名
           // Aggregation value = entry.getValue();//获取聚合
            //设置参数名
            spec.put("k",key);
            //解析aggregation里面的桶，获得聚合参数
            StringTerms options = (StringTerms)entry.getValue();

            List<String> listkey = new ArrayList<>();
            //遍历桶
            options.getBuckets().forEach(option->{
                listkey.add(option.getKeyAsString());//获得每个桶名,即参数
            });

            spec.put("options",listkey);

            result.add(spec);
        }

        return result;
    }

    /**
     * 返回品牌聚合结果集
     * @param brandaggregation
     * @return
     */
    private List<Brand> getBrandsResult(Aggregation brandaggregation) {
        //转换成可以获取桶的类型，LongTerms因为桶的key为Long类型
        LongTerms brands = (LongTerms)brandaggregation;
        //获得所有桶，然后历遍，.stream().map返回新的集合
        return brands.getBuckets().stream().map(bucket -> {
            //根据桶的key，即brandId，根据brandId去数据库查询品牌
            return brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
        }).collect(Collectors.toList());

    }

    /**
     * 返回分类聚合结果集
     * @param categoryaggregation
     * @return
     */
    private List<Map<String, Object>> getCategoriesResult(Aggregation categoryaggregation) {
        //转换可以获取桶的类型
        LongTerms categories = (LongTerms)categoryaggregation;

       // List<Map<String,Object>> Categories = new ArrayList<>();
        //获取所有桶，并且历遍桶获取每个桶的数据
        return categories.getBuckets().stream().map(bucket -> {

            Map<String,Object> category = new HashMap<>();
            long cid = bucket.getKeyAsNumber().longValue();//获取桶的值key，即cid
            //根据cid 去数据库查询对应的分类
            List<String> categoryResult = categoryClient.queryNamesByIds(Arrays.asList(cid));
            //装载数据
            category.put("id",cid);
            category.put("name",categoryResult.get(0));

            return category;

        }).collect(Collectors.toList());//返回新的List集合

    }

    /**
     * 更新同步数据，新增或修改
     * @param id
     */
    public void save(Long id) throws IOException {

        Spu spu = this.goodsClient.querySpuById(id);

        Goods good = this.buildGoods(spu);

        this.goodsReponsitory.save(good);

    }

    /**
     * 更新同步数据，删除
     * @param id
     */
    public void delete(Long id) {

        this.goodsReponsitory.deleteById(id);
    }
}














