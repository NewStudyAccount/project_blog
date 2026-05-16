package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.TableDataInfo;
import com.example.domain.pojo.SysCategory;
import com.example.domain.req.SysCategoryQueryPageReq;
import com.example.domain.vo.PublicCategoryVo;

import java.util.List;

public interface SysCategoryService extends IService<SysCategory> {

    TableDataInfo<SysCategory> querySysCategoryListPage(SysCategoryQueryPageReq pageReq);

    SysCategory queryById(Long id);

    int addSysCategory(SysCategory entity);

    int updateSysCategoryById(SysCategory entity);

    /**
     * 查询公开分类列表
     */
    List<PublicCategoryVo> queryPublicCategoryList();
}
