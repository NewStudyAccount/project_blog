package com.example.controller;

import com.example.domain.Response;
import com.example.domain.TableDataInfo;
import com.example.domain.pojo.SysArticleContent;
import com.example.domain.req.SysArticleContentQueryPageReq;
import com.example.domain.req.SysArticleContentReq;
import com.example.domain.vo.SysArticleContentHtmlVo;
import com.example.domain.vo.SysArticleContentVo;
import com.example.domain.pojo.SysArticleContentHistory;
import com.example.service.SysArticleContentHistoryService;
import com.example.service.SysArticleContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "文章内容")
@RestController
@RequestMapping("/project/sysArticleContent")
public class SysArticleContentController {

    @Autowired
    private SysArticleContentService sysArticleContentService;

    @Autowired
    private SysArticleContentHistoryService historyService;

    @Operation(summary = "分页查询")
    @PostMapping("/list")
    public Response<TableDataInfo<SysArticleContent>> list(@RequestBody SysArticleContentQueryPageReq pageReq) {
        TableDataInfo<SysArticleContent> tableDataInfo = sysArticleContentService.querySysArticleContentListPage(pageReq);
        return Response.success(tableDataInfo);
    }

    @Operation(summary = "根据文章ID查询")
    @GetMapping("/queryByArticleId/{id}")
    public Response<SysArticleContentVo> queryByArticleId(@PathVariable("id") Long id) {
        SysArticleContentVo entity = sysArticleContentService.queryByArticleId(id);
        return Response.success(entity);
    }

    @Operation(summary = "根据文章ID获取HTML格式内容")
    @GetMapping("/queryHtmlByArticleId/{id}")
    public Response<SysArticleContentHtmlVo> queryHtmlByArticleId(@PathVariable("id") Long id) {
        SysArticleContentHtmlVo entity = sysArticleContentService.queryHtmlByArticleId(id);
        return Response.success(entity);
    }

    @Operation(summary = "新增")
    @PostMapping("add")
    public Response<?> addSysArticleContent(@RequestBody SysArticleContentReq sysArticleContentReq) {
        int result = sysArticleContentService.addSysArticleContent(sysArticleContentReq);
        return Response.success(result);
    }

    @Operation(summary = "修改")
    @PostMapping("update")
    public Response<?> updateSysArticleContent(@RequestBody SysArticleContentReq sysArticleContentReq) {
        int result = sysArticleContentService.updateSysArticleContentById(sysArticleContentReq);
        return Response.success(result);
    }

    @Operation(summary = "首次上传文章内容文件")
    @PostMapping("upload")
    public Response<?> uploadContent(@RequestParam("file") MultipartFile file,
                                     @RequestParam("articleId") Long articleId) {
        sysArticleContentService.uploadContent(file, articleId);
        return Response.success("上传成功");
    }

    @Operation(summary = "覆盖上传文章内容文件")
    @PostMapping("uploadOverride")
    public Response<?> uploadOverride(@RequestParam("file") MultipartFile file,
                                      @RequestParam("articleId") Long articleId,
                                      @RequestParam(value = "confirmOverride", defaultValue = "false") boolean confirmOverride) {
        sysArticleContentService.overrideUploadContent(file, articleId, confirmOverride);
        return Response.success("覆盖上传成功");
    }

    @Operation(summary = "查询文章内容版本历史")
    @GetMapping("/history/{articleId}")
    public Response<List<SysArticleContentHistory>> getHistory(@PathVariable("articleId") Long articleId) {
        List<SysArticleContentHistory> history = historyService.queryHistoryByArticleId(articleId);
        return Response.success(history);
    }

}