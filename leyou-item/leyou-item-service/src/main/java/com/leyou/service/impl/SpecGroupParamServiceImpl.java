package com.leyou.service.impl;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.mapper.SpecGroupMapper;
import com.leyou.mapper.SpecParamMapper;
import com.leyou.service.SpecGroupParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecGroupParamServiceImpl implements SpecGroupParamService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据cid 查询分组信息
     * @param cid
     * @return
     */
    @Override
    public List<SpecGroup> queryGroupByCid(Long cid) {

        SpecGroup record = new SpecGroup();
        record.setCid(cid);

       return this.specGroupMapper.select(record);

    }

    /**
     * 查询参数信息
     * @param gid
     * @return
     */
    @Override
    public List<SpecParam> queryParam(Long gid,Long cid,Boolean generic,Boolean searching) {

        SpecParam record = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setSearching(searching);
        record.setGeneric(generic);

        return this.specParamMapper.select(record);
    }

    @Override
    public List<SpecGroup> querySpecsByCid(Long cid) {

        // 查询规格组
        List<SpecGroup> groups = this.queryGroupByCid(cid);
        groups.forEach(g -> {
            // 查询组内参数
            g.setParams(this.queryParam(g.getId(), null, null, null));
        });
        return groups;
    }
}
