package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.TableDataInfo;
import com.example.domain.pojo.SysCategory;
import com.example.domain.req.SysCategoryQueryPageReq;
import com.example.domain.vo.PublicCategoryVo;
import com.example.mapper.SysCategoryMapper;
import com.example.service.SysCategoryService;
import com.example.utils.SnowflakeIdUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysCategoryServiceImpl extends ServiceImpl<SysCategoryMapper, SysCategory> implements SysCategoryService {

    @Override
    public TableDataInfo<SysCategory> querySysCategoryListPage(SysCategoryQueryPageReq pageReq) {
        Page<SysCategory> page = baseMapper.selectPage(pageReq.getPageQuery().build(), null);
        return TableDataInfo.build(page);
    }


    @Override
    public SysCategory queryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public int addSysCategory(SysCategory entity) {
        long blogNextId = SnowflakeIdUtil.blogNextId();
        entity.setId(blogNextId);
        return baseMapper.insert(entity);
    }

    @Override
    public int updateSysCategoryById(SysCategory entity) {
        return baseMapper.updateById(entity);
    }

    @Override
    public List<PublicCategoryVo> queryPublicCategoryList() {
        LambdaQueryWrapper<SysCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysCategory::getId, SysCategory::getName);
        wrapper.orderByAsc(SysCategory::getId);
        List<SysCategory> list = baseMapper.selectList(wrapper);

        return list.stream().map(category -> {
            PublicCategoryVo vo = new PublicCategoryVo();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());
    }

}
