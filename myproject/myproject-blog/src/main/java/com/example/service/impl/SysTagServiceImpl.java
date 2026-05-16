package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.TableDataInfo;
import com.example.domain.pojo.SysTag;
import com.example.domain.req.SysTagQueryPageReq;
import com.example.domain.vo.PublicTagVo;
import com.example.mapper.SysTagMapper;
import com.example.service.SysTagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysTagServiceImpl extends ServiceImpl<SysTagMapper, SysTag> implements SysTagService {

    @Override
    public TableDataInfo<SysTag> querySysTagListPage(SysTagQueryPageReq pageReq) {
        Page<SysTag> page = baseMapper.selectPage(pageReq.getPageQuery().build(), null);
        return TableDataInfo.build(page);
    }


    @Override
    public SysTag queryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public int addSysTag(SysTag entity) {
        return baseMapper.insert(entity);
    }

    @Override
    public int updateSysTagById(SysTag entity) {
        return baseMapper.updateById(entity);
    }

    @Override
    public List<PublicTagVo> queryPublicTagList() {
        LambdaQueryWrapper<SysTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysTag::getId, SysTag::getName);
        wrapper.orderByAsc(SysTag::getId);
        List<SysTag> list = baseMapper.selectList(wrapper);

        return list.stream().map(tag -> {
            PublicTagVo vo = new PublicTagVo();
            BeanUtils.copyProperties(tag, vo);
            return vo;
        }).collect(Collectors.toList());
    }

}
