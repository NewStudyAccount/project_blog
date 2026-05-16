package com.example.controller;

import com.example.domain.Response;
import com.example.domain.TableDataInfo;
import com.example.domain.req.PublicArticleListReq;
import com.example.domain.req.SysArticleQueryPageReq;
import com.example.domain.req.SysArticleReq;
import com.example.domain.vo.PublicArticleVo;
import com.example.domain.vo.SysArticleVo;
import com.example.service.SysArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "文章表")
@RestController
@RequestMapping("/project/sysArticle")
public class SysArticleController {

    @Autowired
    private SysArticleService sysArticleService;

    @Operation(summary = "分页查询")
    @PostMapping("/list")
    public Response<TableDataInfo<SysArticleVo>> list(@RequestBody SysArticleQueryPageReq pageReq) {
        TableDataInfo<SysArticleVo> tableDataInfo = sysArticleService.querySysArticleListPage(pageReq);
        return Response.success(tableDataInfo);
    }

    @Operation(summary = "根据ID查询")
    @GetMapping("/{id}")
    public Response<SysArticleVo> queryById(@PathVariable("id") Long id) {
        SysArticleVo vo = sysArticleService.queryVoById(id);
        return Response.success(vo);
    }

    @Operation(summary = "新增")
    @PostMapping("add")
    public Response<?> addSysArticle(@RequestBody SysArticleReq req) {
        int result = sysArticleService.addSysArticle(req);
        return Response.success(result);
    }

    @Operation(summary = "修改")
    @PostMapping("update")
    public Response<?> updateSysArticle(@RequestBody SysArticleReq req) {
        int result = sysArticleService.updateSysArticleById(req);
        return Response.success(result);
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Response<?> delete(@PathVariable("id") Long id) {
        boolean result = sysArticleService.removeById(id);
        return Response.success(result);
    }

    @Operation(summary = "公开文章列表（无需登录）")
    @PostMapping("/public/list")
    public Response<TableDataInfo<PublicArticleVo>> publicList(@RequestBody PublicArticleListReq req) {
        TableDataInfo<PublicArticleVo> tableDataInfo = sysArticleService.queryPublicArticleList(req);
        return Response.success(tableDataInfo);
    }

    @Operation(summary = "公开根据ID查询文章详情（无需登录）")
    @GetMapping("/public/{id}")
    public Response<SysArticleVo> publicQueryById(@PathVariable("id") Long id) {
        SysArticleVo vo = sysArticleService.queryVoById(id);
        return Response.success(vo);
    }
}
