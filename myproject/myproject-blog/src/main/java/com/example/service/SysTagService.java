package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.TableDataInfo;
import com.example.domain.pojo.SysTag;
import com.example.domain.req.SysTagQueryPageReq;
import com.example.domain.vo.PublicTagVo;

import java.util.List;

public interface SysTagService extends IService<SysTag> {

    TableDataInfo<SysTag> querySysTagListPage(SysTagQueryPageReq pageReq);

    SysTag queryById(Long id);

    int addSysTag(SysTag entity);

    int updateSysTagById(SysTag entity);

    /**
     * 查询公开标签列表
     */
    List<PublicTagVo> queryPublicTagList();
}
