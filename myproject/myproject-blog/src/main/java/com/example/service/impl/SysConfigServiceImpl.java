package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.pojo.SysConfig;
import com.example.mapper.SysConfigMapper;
import com.example.service.SysConfigService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    @Override
    public Map<String, String> getPublicConfig() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysConfig::getConfigKey, SysConfig::getConfigValue);
        List<SysConfig> list = baseMapper.selectList(wrapper);

        Map<String, String> configMap = new HashMap<>();
        for (SysConfig config : list) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        return configMap;
    }
}
