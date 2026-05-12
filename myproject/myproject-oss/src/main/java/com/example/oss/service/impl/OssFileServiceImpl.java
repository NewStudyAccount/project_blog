package com.example.oss.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.PageQuery;
import com.example.domain.TableDataInfo;
import com.example.oss.domain.SysOssConfig;
import com.example.oss.domain.SysOssFile;
import com.example.oss.domain.req.sysOssFile.SysOssFileQueryPageReq;
import com.example.oss.factory.OssClientFactory;
import com.example.oss.mapper.OssFileMapper;
import com.example.oss.service.OssConfigService;
import com.example.oss.service.OssFileService;
import com.example.utils.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


@Service
public class OssFileServiceImpl extends ServiceImpl<OssFileMapper, SysOssFile> implements OssFileService {


    @Autowired
    private OssClientFactory ossClientFactory;

    @Autowired
    private OssConfigService ossConfigService;

    public String newFileName() {
        return "";
    }


    @Override
    public String uploadFile(MultipartFile file) {
        try {
            SysOssConfig sysOssConfig = ossConfigService.getActiveConfig();
            if (sysOssConfig == null) {
                throw new RuntimeException("未找到有效的OSS配置");
            }

            String originalFilename = file.getOriginalFilename();
            String[] split = originalFilename.split("\\.");
            String newFileName = UUID.randomUUID().toString() + "." + split[1];

            String contentType = file.getContentType();
            ossClientFactory.uploadFile(sysOssConfig, newFileName, contentType, file.getBytes());

            SysOssFile sysOssFile = new SysOssFile(newFileName, originalFilename, split[1], newFileName, contentType);
            insertSysOssFile(sysOssFile);

            return buildFullUrl(newFileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String configName) {
        return "";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadFile(Long ossId, String fileName, String contentType, byte[] data) {
        SysOssConfig sysOssConfig = ossConfigService.getActiveConfig();
        if (sysOssConfig == null) {
            throw new RuntimeException("未找到有效的OSS配置");
        }

        String[] split = fileName.split("\\.");
        String newFileName = UUID.randomUUID().toString() + "." + split[1];
        if (ossId != null) {
            SysOssFile existingFile = this.baseMapper.selectById(ossId);
            newFileName = existingFile.getFileName();
        }

        ossClientFactory.uploadFile(sysOssConfig, newFileName, contentType, data);

        SysOssFile sysOssFile = new SysOssFile(newFileName, fileName, split[1], newFileName, contentType);

        if (ossId == null) {
            insertSysOssFile(sysOssFile);
        } else {
            sysOssFile.setOssId(ossId);
            this.baseMapper.updateById(sysOssFile);
        }

        return sysOssFile.getOssId();
    }

    @Override
    public String downloadFile(String fileName) {
        return "";
    }

    @Override
    public byte[] downloadFileContent(String fileUrl) {
        try {
            SysOssConfig sysOssConfig = ossConfigService.getActiveConfig();
            if (sysOssConfig == null) {
                throw new RuntimeException("未找到有效的OSS配置");
            }
            return ossClientFactory.downloadFile(sysOssConfig, fileUrl);
        } catch (Exception e) {
            throw new RuntimeException("下载文件内容失败: " + e.getMessage(), e);
        }
    }

    @Override
    public TableDataInfo<SysOssFile> querySysOssFileListPage(SysOssFileQueryPageReq sysOssFileQueryPageReq) {
        PageQuery pageQuery = sysOssFileQueryPageReq.getPageQuery();
        Page<SysOssFile> sysOssFilePage = this.baseMapper.selectPage(pageQuery.build(), null);
        sysOssFilePage.getRecords().forEach(this::fillFullUrl);
        return TableDataInfo.build(sysOssFilePage);
    }

    @Override
    public SysOssFile queryById(Long id) {
        SysOssFile entity = this.baseMapper.selectById(id);
        if (entity != null) {
            fillFullUrl(entity);
        }
        return entity;
    }

    @Override
    public int deleteById(Long id) {
        return this.baseMapper.deleteById(id);
    }


    private String buildFullUrl(String fileName) {
        SysOssConfig config = ossConfigService.getActiveConfig();
        if (config == null) {
            return fileName;
        }
        return config.getEndpoint() + "/" + config.getBucketName() + "/" + fileName;
    }

    private void fillFullUrl(SysOssFile sysOssFile) {
        sysOssFile.setFullUrl(buildFullUrl(sysOssFile.getFileUrl()));
    }

    public void insertSysOssFile(SysOssFile sysOssFile) {
        long ossNextId = SnowflakeIdUtil.ossNextId();
        sysOssFile.setOssId(ossNextId);
        this.baseMapper.insert(sysOssFile);
    }

}
