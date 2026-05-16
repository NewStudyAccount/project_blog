package com.example.controller;

import com.example.domain.Response;
import com.example.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "站点配置")
@RestController
@RequestMapping("/project/sysConfig")
public class SysConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    @Operation(summary = "获取公开配置信息")
    @GetMapping("/public/info")
    public Response<Map<String, String>> getPublicConfig() {
        Map<String, String> config = sysConfigService.getPublicConfig();
        return Response.success(config);
    }
}
