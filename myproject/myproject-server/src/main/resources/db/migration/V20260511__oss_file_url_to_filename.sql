-- 将 sys_oss_file.file_url 从完整 URL 迁移为仅存储文件名
-- 执行前请确保已部署新版本代码

UPDATE sys_oss_file
SET file_url = SUBSTRING_INDEX(file_url, '/', -1)
WHERE file_url LIKE '%/%';
