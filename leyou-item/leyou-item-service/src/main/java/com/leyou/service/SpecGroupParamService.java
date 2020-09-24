package com.leyou.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import java.util.List;

public interface SpecGroupParamService {

    List<SpecGroup> queryGroupByCid(Long cid);

    List<SpecParam> queryParam(Long gid,Long cid,Boolean generic,Boolean searching);

    List<SpecGroup> querySpecsByCid(Long cid);
}
