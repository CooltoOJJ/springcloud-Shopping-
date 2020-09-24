package com.leyou.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.mapper.BrandMapper;
import com.leyou.service.BrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper mapper;

    @Override
    public PageResult<Brand> queryBrandByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {

        //初始化
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();

        //通用Mapper模糊查询
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("name","%"+key+"%").orEqualTo("letter",key);
        }
        //分页查询
        PageHelper.startPage(page,rows);

        //排序
        if(StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy +" " + (desc ? "desc" : "asc"));
        }
        //查询语句
        List<Brand> brands = mapper.selectByExample(example);
        //查询结果放入PageInfo类里面封装
        PageInfo<Brand> listPageInfo = new PageInfo<>(brands);

        return new PageResult<>(listPageInfo.getTotal(),listPageInfo.getList());
    }

    @Override
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {

        //插入新增品牌表
        this.mapper.insertSelective(brand);

        //插入中间表
        cids.forEach(cid ->{
            this.mapper.insertCategoryBrand(cid,brand.getId());
        });

    }

    /**
     * 根据cid查询品牌
     * @param cid
     * @return
     */
    @Override
    public List<Brand> queryBrandByCid(Long cid) {

        //根据中间表cid查询对应品牌id
        return this.mapper.selectBrandsByCid(cid);
    }

    @Override
    public Brand queryBrandById(Long id) {
        return this.mapper.selectByPrimaryKey(id);
    }


}
