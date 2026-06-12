package com.zhunong.mall.controller;

import com.zhunong.mall.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.qiniu.util.Auth;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${qiniu.access-key}")
    private String accessKey;

    @Value("${qiniu.secret-key}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.domain}")
    private String domain;

    /**
     * 获取七牛云上传Token
     * 使用七牛云SDK生成真实的上传凭证
     */
    @GetMapping("/qiniu-token")
    public Result<Map<String, Object>> getQiniuToken() {
        try {
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);

            Map<String, Object> result = new HashMap<>();
            result.put("token", upToken);
            result.put("domain", domain);
            result.put("bucket", bucket);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取上传凭证失败: " + e.getMessage());
        }
    }

    /**
     * 文件上传接口（后端中转模式）
     * 保存到本地uploads目录并返回访问URL
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error(400, "请选择要上传的文件");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error(400, "只支持图片文件");
            }

            String originalName = file.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

            // 确保上传目录存在
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File destFile = new File(uploadDir + fileName);
            file.transferTo(destFile);

            // 返回可访问的URL（通过后端的/file/接口）
            String fileUrl = "/api/common/file/" + fileName;

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileName", fileName);

            return Result.success(result);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 文件访问接口
     * 用于获取已上传的图片文件
     */
    @GetMapping("/file/{fileName}")
    public Resource getFile(@PathVariable String fileName) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            Path filePath = Paths.get(uploadDir + fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
