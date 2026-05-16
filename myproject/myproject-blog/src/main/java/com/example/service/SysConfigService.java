package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.pojo.SysConfig;

import java.util.Map;

public interface SysConfigService extends IService<SysConfig> {

    /**
     * 获取公开配置信息
     * @return 配置键值对
     */
    Map<String, String> getPublicConfig();
}
